package com.example.order_service.api.order.facade;

import com.example.order_service.api.common.dto.PageDto;
import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.ErrorCode;
import com.example.order_service.api.common.exception.OrderErrorCode;
import com.example.order_service.api.common.exception.PaymentErrorCode;
import com.example.order_service.api.common.util.AsyncUtil;
import com.example.order_service.api.order.controller.dto.request.OrderSearchCondition;
import com.example.order_service.api.order.domain.model.OrderFailureCode;
import com.example.order_service.api.order.domain.model.OrderStatus;
import com.example.order_service.api.order.domain.service.*;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext;
import com.example.order_service.api.order.domain.service.dto.command.PaymentCreationContext;
import com.example.order_service.api.order.domain.service.dto.result.*;
import com.example.order_service.api.order.facade.dto.OrderPreparationData;
import com.example.order_service.api.order.facade.dto.command.CreateOrderCommand;
import com.example.order_service.api.order.facade.dto.command.CreateOrderItemCommand;
import com.example.order_service.api.order.facade.dto.result.CreateOrderResponse;
import com.example.order_service.api.order.facade.dto.result.OrderDetailResponse;
import com.example.order_service.api.order.facade.dto.result.OrderListResponse;
import com.example.order_service.api.order.facade.event.*;
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
        OrderDto orderDto = orderService.preparePaymentWaiting(orderNo);
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
        // 결제 가능한 상태인지 검증
        validBeforePayment(order, amount);
        // 결제 서비스 결제 승인 요청
        OrderPaymentInfo orderPaymentInfo = confirmPayment(order.getOrderNo(), paymentKey, order.getOrderPriceInfo().getFinalPaymentAmount());
        PaymentCreationContext paymentContext = mapper.mapPaymentCreationContext(orderPaymentInfo);
        OrderDto orderDto = orderService.completePayment(paymentContext);

        //TODO 리팩터링
        eventPublisher.publishEvent(PaymentCompletedEvent.of(orderDto.getOrderNo(), orderDto.getOrderer().getUserId(),
                orderDto.getOrderItems().stream().map(i -> i.getOrderedProduct().getProductVariantId()).toList()));
        return OrderDetailResponse.from(orderDto);
    }

    public OrderDetailResponse getOrder(Long userId, String orderNo) {
        OrderDto order = orderService.getOrder(orderNo, userId);
        return OrderDetailResponse.from(order);
    }

    public PageDto<OrderListResponse> getOrders(Long userId, OrderSearchCondition condition){
        Page<OrderDto> orders = orderService.getOrders(userId, condition);
        return PageDto.of(orders, OrderListResponse::from);
    }

    private OrderPaymentInfo confirmPayment(String orderNo, String paymentKey, Long amount) {
        try {
            // 결제 서비스를 호출해 결제를 진행
            return orderPaymentService.confirmOrderPayment(orderNo, paymentKey, amount);
        } catch (BusinessException e) {
            // 결제 서비스 호출중 예외 발생시 주문 상태를 변경하고 saga 롤백을 위한 이벤트를 발행
            OrderFailureCode orderFailureCode = mapToOrderFailureCode(e.getErrorCode());
            OrderDto failOrderDto = orderService.failPayment(orderNo, orderFailureCode);
            eventPublisher.publishEvent(PaymentFailedEvent
                    .of(failOrderDto.getOrderNo(),
                            failOrderDto.getOrderer().getUserId(),
                            e.getErrorCode().name(),
                            e.getErrorCode().getMessage()));
            throw e;
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

    private OrderFailureCode mapToOrderFailureCode(ErrorCode errorCode) {
        if (errorCode == PaymentErrorCode.PAYMENT_INSUFFICIENT_BALANCE) {
            return OrderFailureCode.PAYMENT_INSUFFICIENT_BALANCE;
        }
        if (errorCode == PaymentErrorCode.PAYMENT_TIMEOUT) {
            return OrderFailureCode.PAYMENT_TIMEOUT;
        }
        if (errorCode == PaymentErrorCode.PAYMENT_NOT_FOUND) {
            return OrderFailureCode.PAYMENT_NOT_FOUND;
        }
        return OrderFailureCode.UNKNOWN;
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
