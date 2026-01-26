package com.example.order_service.api.order.facade;

import com.example.order_service.api.common.dto.PageDto;
import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.CommonErrorCode;
import com.example.order_service.api.common.exception.ErrorCode;
import com.example.order_service.api.common.exception.OrderErrorCode;
import com.example.order_service.api.common.util.AsyncUtil;
import com.example.order_service.api.order.controller.dto.request.OrderSearchCondition;
import com.example.order_service.api.order.domain.model.OrderFailureCode;
import com.example.order_service.api.order.domain.model.OrderStatus;
import com.example.order_service.api.order.domain.service.*;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext;
import com.example.order_service.api.order.domain.service.dto.command.PaymentCreationCommand;
import com.example.order_service.api.order.domain.service.dto.result.*;
import com.example.order_service.api.order.facade.dto.OrderPreparationData;
import com.example.order_service.api.order.facade.dto.command.CreateOrderCommand;
import com.example.order_service.api.order.facade.dto.command.CreateOrderItemCommand;
import com.example.order_service.api.order.facade.dto.result.CreateOrderResponse;
import com.example.order_service.api.order.facade.dto.result.OrderDetailResponse;
import com.example.order_service.api.order.facade.dto.result.OrderListResponse;
import com.example.order_service.api.order.facade.event.OrderCreatedEvent;
import com.example.order_service.api.order.facade.event.OrderFailedEvent;
import com.example.order_service.api.order.facade.event.OrderPaymentReadyEvent;
import com.example.order_service.api.order.infrastructure.client.payment.dto.response.TossPaymentConfirmResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderPaymentService orderPaymentService;
    private final OrderUserService orderUserService;
    private final OrderProductService orderProductService;
    private final OrderCouponService orderCouponService;
    private final OrderPriceCalculator calculator;
    private final OrderCreationContextMapper mapper;
    private final OrderService orderService;
    private final ApplicationEventPublisher eventPublisher;

    public CreateOrderResponse initialOrder(CreateOrderCommand command){
        // 중복 상품이 있는지 검증
        validateUniqueItems(command.getOrderItemCommands());
        //CompletableFuture 을 사용해서 상품, 유저 요청을 비동기로 동시에 조회
        OrderPreparationData orderPreparationData = getOrderPreparationData(command);
        //주문 상품 가격 정보 계산
        OrderProductAmount productAmount = calculator.calculateItemAmounts(command.getOrderItemCommands(), orderPreparationData.getProducts());
        OrderCouponInfo coupon = orderCouponService.calculateCouponDiscount(command.getUserId(), command.getCouponId(), productAmount);
        //할인 적용 최종 금액 계산
        CalculatedOrderAmounts calculatedOrderAmounts = calculator.calculateOrderPrice(productAmount, coupon, command.getPointToUse(), command.getExpectedPrice());
        // 주문 생성 Context 매핑
        OrderCreationContext creationContext =
                mapper.mapOrderCreationContext(orderPreparationData.getUser(), calculatedOrderAmounts, coupon, command, orderPreparationData.getProducts());
        // 주문 저장
        OrderDto orderDto = orderService.saveOrder(creationContext);
        //SAGA 진행을 위한 이벤트 발행
        eventPublisher.publishEvent(OrderCreatedEvent.from(orderDto));
        return CreateOrderResponse.from(orderDto);
    }

    public void preparePayment(String orderNo) {
        OrderDto orderDto = orderService.changeOrderStatus(orderNo, OrderStatus.PAYMENT_WAITING);
        // SSE 메시지 전송을 위한 이벤트 발행
        eventPublisher.publishEvent(OrderPaymentReadyEvent.from(orderDto));
    }

    public void processOrderFailure(String orderNo, OrderFailureCode orderFailureCode){
        OrderDto orderDto = orderService.canceledOrder(orderNo, orderFailureCode);
        // SSE 메시지 전송을 위한 이벤트 발행
        eventPublisher.publishEvent(OrderFailedEvent.from(orderDto));
    }

    public OrderDetailResponse confirmOrderPayment(String orderNo, Long userId, String paymentKey, Long amount) {
        OrderDto order = orderService.getOrder(orderNo, userId);
        validBeforePayment(order, amount);
        try {
            TossPaymentConfirmResponse tossPaymentConfirmResponse = orderPaymentService.confirmOrderPayment(order.getOrderNo(), paymentKey, amount);
        } catch (Exception e) {
            log.error("TEST");
        }
        return null;
    }

    public OrderDetailResponse getOrder(Long userId, String orderNo) {
        OrderDto order = orderService.getOrder(orderNo, userId);
        return OrderDetailResponse.from(order);
    }

    public PageDto<OrderListResponse> getOrders(Long userId, OrderSearchCondition condition){
        Page<OrderDto> orders = orderService.getOrders(userId, condition);
        return PageDto.of(orders, OrderListResponse::from);
    }

    // 토스 결제 승인 실행
    private TossPaymentConfirmResponse executePaymentConfirmRequest(OrderDto orderDto, String paymentKey) {
        try {
            return orderPaymentService.confirmOrderPayment(orderDto.getOrderNo(), paymentKey, orderDto.getOrderPriceInfo().getFinalPaymentAmount());
        } catch (BusinessException e) {
            // 결제 중 예외가 발생한 경우 주문 상태 변경 후 Saga 보상 로직 실행
            handlePaymentFailure(orderDto.getOrderNo(), e.getErrorCode());
            throw e;
        }
    }

    // 결제 후 주문 완료 실행
    private OrderDetailResponse completeOrderWithCompensation(OrderDto orderDto, TossPaymentConfirmResponse response, String paymentKey) {
        try {
            return processOrderCompletion(response);
        } catch (Exception e) {
            // 주문 완료 DB 상태 변경중 예외 발생시 결제는 완료되었으므로 결제 환불 요청 후 SAGA 보상 실행
            compensatePayment(paymentKey, "시스템 에러");
            handlePaymentFailure(orderDto.getOrderNo(), CommonErrorCode.INTERNAL_ERROR);
            throw new BusinessException(CommonErrorCode.INTERNAL_ERROR);
        }
    }

    private void validBeforePayment(OrderDto order, Long amount) {
        if (!order.getStatus().equals(OrderStatus.PAYMENT_WAITING)) {
            throw new BusinessException(OrderErrorCode.ORDER_NOT_PAYABLE);
        }

        if (order.getOrderPriceInfo().getFinalPaymentAmount() != amount) {
            throw new BusinessException(OrderErrorCode.ORDER_PRICE_MISMATCH);
        }
    }

    private OrderDetailResponse processOrderCompletion(TossPaymentConfirmResponse response) {
        OrderDto completedOrder = orderService.completedOrder(PaymentCreationCommand.from(response));
//        List<Long> productVariantIds = completedOrder.getOrderItemDtoList().stream()
//                .map(OrderItemDto::getProductVariantId).toList();
//        eventPublisher.publishEvent(PaymentResultEvent.of(completedOrder.getOrderNo(), completedOrder.getUserId(), OrderEventStatus.SUCCESS, null,
//                productVariantIds));
        return OrderDetailResponse.from(completedOrder);
    }

    private void compensatePayment(String paymentKey, String cancelReason) {
        try {
            orderPaymentService.cancelPayment(paymentKey, cancelReason, null);
        } catch (Exception e) {
            log.error("결제 : [치명적 오류] 시스템 오류로 발생한 결제 환불 실패");
        }
    }

    private void handlePaymentFailure(String orderNo, ErrorCode errorCode) {
        OrderFailureCode failureCode = OrderFailureCode.fromErrorCode(errorCode);
//        OrderDto canceledOrder = orderService.canceledOrder(orderNo, failureCode);
//        eventPublisher.publishEvent(PaymentResultEvent.of(canceledOrder.getOrderNo(), canceledOrder.getUserId(), OrderEventStatus.FAILURE,
//                failureCode, null));
    }

    private void validateUniqueItems(List<CreateOrderItemCommand> items) {
        Set<Long> setIds = items.stream().map(CreateOrderItemCommand::getProductVariantId).collect(Collectors.toSet());
        if (items.size() != setIds.size()) {
            throw new BusinessException(OrderErrorCode.ORDER_DUPLICATE_ORDER_PRODUCT);
        }
    }

    // 유저정보, 상품 정보를 비동기로 동시 조회
    private OrderPreparationData getOrderPreparationData(CreateOrderCommand command) {
        CompletableFuture<OrderUserInfo> userFuture = CompletableFuture.supplyAsync(() -> orderUserService.getUser(command.getUserId(), command.getPointToUse()));
        CompletableFuture<List<OrderProductInfo>> productFuture = CompletableFuture.supplyAsync(() -> orderProductService.getProducts(command.getOrderItemCommands()));
        CompletableFuture.allOf(userFuture, productFuture).join();

        return OrderPreparationData.builder()
                .user(AsyncUtil.join(userFuture))
                .products(AsyncUtil.join(productFuture))
                .build();
    }
}
