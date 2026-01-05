package com.example.order_service.api.order.application;

import com.example.order_service.api.common.dto.PageDto;
import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.CommonErrorCode;
import com.example.order_service.api.common.exception.ErrorCode;
import com.example.order_service.api.common.exception.OrderErrorCode;
import com.example.order_service.api.common.util.AsyncUtil;
import com.example.order_service.api.order.application.dto.command.CreateOrderDto;
import com.example.order_service.api.order.application.dto.result.CreateOrderResponse;
import com.example.order_service.api.order.application.dto.result.OrderDetailResponse;
import com.example.order_service.api.order.application.dto.result.OrderListResponse;
import com.example.order_service.api.order.application.event.OrderCreatedEvent;
import com.example.order_service.api.order.application.event.OrderEventStatus;
import com.example.order_service.api.order.application.event.OrderResultEvent;
import com.example.order_service.api.order.application.event.PaymentResultEvent;
import com.example.order_service.api.order.controller.dto.request.OrderSearchCondition;
import com.example.order_service.api.order.domain.model.OrderFailureCode;
import com.example.order_service.api.order.domain.model.OrderStatus;
import com.example.order_service.api.order.domain.model.vo.PriceCalculateResult;
import com.example.order_service.api.order.domain.service.OrderDomainService;
import com.example.order_service.api.order.domain.service.OrderPriceCalculator;
import com.example.order_service.api.order.domain.service.dto.command.CreateOrderCommand;
import com.example.order_service.api.order.domain.service.dto.command.PaymentCreationCommand;
import com.example.order_service.api.order.domain.service.dto.result.ItemCalculationResult;
import com.example.order_service.api.order.domain.service.dto.result.OrderDto;
import com.example.order_service.api.order.domain.service.dto.result.OrderItemDto;
import com.example.order_service.api.order.infrastructure.OrderExternalAdaptor;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponDiscountResponse;
import com.example.order_service.api.order.infrastructure.client.payment.dto.response.TossPaymentConfirmResponse;
import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse;
import com.example.order_service.api.order.infrastructure.client.user.dto.OrderUserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderApplicationService {

    private final OrderExternalAdaptor orderExternalAdaptor;
    private final OrderPriceCalculator calculator;
    private final OrderDtoMapper mapper;
    private final OrderDomainService orderDomainService;
    private final ApplicationEventPublisher eventPublisher;

    public CreateOrderResponse placeOrder(CreateOrderDto dto){
        //CompletableFuture 을 사용해서 상품, 유저 요청을 비동기로 동시에 조회
        CompletableFuture<OrderUserResponse> userFuture = CompletableFuture.supplyAsync(() -> orderExternalAdaptor.getOrderUser(dto.getUserId()));
        CompletableFuture<List<OrderProductResponse>> productFuture = CompletableFuture.supplyAsync(() -> orderExternalAdaptor.getOrderProducts(dto.getOrderItemDtoList()));
        CompletableFuture.allOf(userFuture, productFuture).join();
        OrderUserResponse user = AsyncUtil.join(userFuture);
        List<OrderProductResponse> products = AsyncUtil.join(productFuture);
        //주문 상품 가격 정보 계산
        ItemCalculationResult itemResult = calculator.calculateItemAmounts(dto.getOrderItemDtoList(), products);
        OrderCouponDiscountResponse coupon = orderExternalAdaptor.getCoupon(dto.getUserId(), dto.getCouponId(), itemResult.getSubTotalPrice());
        //할인 적용 최종 금액 계산
        PriceCalculateResult priceResult = calculator
                .calculateFinalPrice(dto.getPointToUse(), itemResult, dto.getExpectedPrice(), user, coupon);
        CreateOrderCommand creationContext = mapper.assembleOrderCommand(dto, user, products, priceResult);
        OrderDto orderDto = orderDomainService.saveOrder(creationContext);
        eventPublisher.publishEvent(OrderCreatedEvent.from(orderDto));
        return CreateOrderResponse.of(orderDto);
    }

    public void preparePayment(String orderNo) {
        OrderDto orderDto = orderDomainService.changeOrderStatus(orderNo, OrderStatus.PAYMENT_WAITING);
        eventPublisher.publishEvent(OrderResultEvent.paymentReady(orderDto));
    }

    public void processOrderFailure(String orderNo, OrderFailureCode orderFailureCode){
        OrderDto orderDto = orderDomainService.canceledOrder(orderNo, orderFailureCode);
        eventPublisher.publishEvent(OrderResultEvent.failure(orderDto));
    }

    public OrderDetailResponse finalizeOrder(String orderNo, Long userId, String paymentKey, Long amount) {
        OrderDto order = orderDomainService.getOrder(orderNo, userId);
        validBeforePayment(order, amount);
        TossPaymentConfirmResponse confirmResponse = executePaymentConfirmRequest(order, paymentKey);
        return completeOrderWithCompensation(order, confirmResponse, paymentKey);
    }

    public OrderDetailResponse getOrder(Long userId, String orderNo) {
        OrderDto order = orderDomainService.getOrder(orderNo, userId);
        return OrderDetailResponse.from(order);
    }

    public PageDto<OrderListResponse> getOrders(Long userId, OrderSearchCondition condition){
        Page<OrderDto> orders = orderDomainService.getOrders(userId, condition);
        return PageDto.of(orders, OrderListResponse::from);
    }

    // 토스 결제 승인 실행
    private TossPaymentConfirmResponse executePaymentConfirmRequest(OrderDto orderDto, String paymentKey) {
        try {
            return orderExternalAdaptor.confirmOrderPayment(orderDto.getOrderNo(), paymentKey, orderDto.getOrderPriceInfo().getFinalPaymentAmount());
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
        OrderDto completedOrder = orderDomainService.completedOrder(PaymentCreationCommand.from(response));
        List<Long> productVariantIds = completedOrder.getOrderItemDtoList().stream()
                .map(OrderItemDto::getProductVariantId).toList();
        eventPublisher.publishEvent(PaymentResultEvent.of(completedOrder.getOrderNo(), completedOrder.getUserId(), OrderEventStatus.SUCCESS, null,
                productVariantIds));
        return OrderDetailResponse.from(completedOrder);
    }

    private void compensatePayment(String paymentKey, String cancelReason) {
        try {
            orderExternalAdaptor.cancelPayment(paymentKey, cancelReason, null);
        } catch (Exception e) {
            log.error("결제 : [치명적 오류] 시스템 오류로 발생한 결제 환불 실패");
        }
    }

    private void handlePaymentFailure(String orderNo, ErrorCode errorCode) {
        OrderFailureCode failureCode = OrderFailureCode.fromErrorCode(errorCode);
        OrderDto canceledOrder = orderDomainService.canceledOrder(orderNo, failureCode);
        eventPublisher.publishEvent(PaymentResultEvent.of(canceledOrder.getOrderNo(), canceledOrder.getUserId(), OrderEventStatus.FAILURE,
                failureCode, null));
    }
}
