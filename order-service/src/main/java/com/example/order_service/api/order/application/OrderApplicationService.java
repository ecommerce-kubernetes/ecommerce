package com.example.order_service.api.order.application;

import com.example.order_service.api.common.dto.PageDto;
import com.example.order_service.api.common.exception.*;
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
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext;
import com.example.order_service.api.order.domain.service.dto.command.OrderItemSpec;
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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderApplicationService {

    private final OrderExternalAdaptor orderExternalAdaptor;
    private final OrderPriceCalculator calculator;
    private final OrderDomainService orderDomainService;
    private final ApplicationEventPublisher eventPublisher;

    //CompletableFuture 을 사용한 비동기 호출 고려
    public CreateOrderResponse placeOrder(CreateOrderDto dto){
        //주문 유저 조회
        OrderUserResponse user = orderExternalAdaptor.getOrderUser(dto.getUserId());
        //주문 상품 목록 조회
        List<OrderProductResponse> products = orderExternalAdaptor.getOrderProducts(dto.getOrderItemDtoList());
        //주문 상품 가격 정보 계산
        ItemCalculationResult itemResult = calculator.calculateItemAmounts(dto.getOrderItemDtoList(), products);
        OrderCouponDiscountResponse coupon = orderExternalAdaptor.getCoupon(dto.getUserId(), dto.getCouponId(), itemResult.getSubTotalPrice());
        //할인 적용 최종 금액 계산
        PriceCalculateResult priceResult = calculator
                .calculateFinalPrice(dto.getPointToUse(), itemResult, dto.getExpectedPrice(), user, coupon);

        OrderCreationContext creationContext =
                assembleOrderContext(dto, user, products, priceResult);
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

        List<OrderListResponse> content = orders.getContent().stream().map(OrderListResponse::from).toList();
        return PageDto.of(orders, content);
    }

    private OrderCreationContext assembleOrderContext(CreateOrderDto dto,
                                                      OrderUserResponse user,
                                                      List<OrderProductResponse> products,
                                                      PriceCalculateResult priceResult){
        Map<Long, OrderProductResponse> productMap = products.stream()
                .collect(Collectors.toMap(OrderProductResponse::getProductVariantId, Function.identity()));

        List<OrderItemSpec> itemSpecs = dto.getOrderItemDtoList().stream()
                .map(item -> {
                    OrderProductResponse product = productMap.get(item.getProductVariantId());
                    return OrderItemSpec.of(product, item.getQuantity());
                }).toList();
        return OrderCreationContext.of(user.getUserId(), itemSpecs, priceResult, dto.getDeliveryAddress());
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
            // 주문 완료 DB 상태 변경중 예외 발생시 결제는 완료되었으므로 결제 환불 요청 후 SAGA 환불 실행
            compensatePayment(paymentKey);
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

    private void compensatePayment(String paymentKey) {
        try {
            orderExternalAdaptor.cancelPayment(paymentKey);
        } catch (Exception e) {
            log.error("결제 : [치명적 오류] 시스템 오류로 발생한 결제 환불 실패");
        }
    }

    private void handlePaymentFailure(String orderNo, ErrorCode errorCode) {
        OrderFailureCode failureCode = mapToOrderFailureCode(errorCode);
        OrderDto canceledOrder = orderDomainService.canceledOrder(orderNo, failureCode);
        eventPublisher.publishEvent(PaymentResultEvent.of(canceledOrder.getOrderNo(), canceledOrder.getUserId(), OrderEventStatus.FAILURE,
                failureCode, null));
    }

    private OrderFailureCode mapToOrderFailureCode(ErrorCode errorCode) {
        if (errorCode instanceof PaymentErrorCode paymentErrorCode) {
            return switch (paymentErrorCode) {
                case PAYMENT_INSUFFICIENT_BALANCE -> OrderFailureCode.PAYMENT_INSUFFICIENT_BALANCE;
                case PAYMENT_TIMEOUT -> OrderFailureCode.PAYMENT_TIMEOUT;
                case PAYMENT_ALREADY_PROCEED_PAYMENT -> OrderFailureCode.ALREADY_PROCEED_PAYMENT;
                case PAYMENT_NOT_FOUND -> OrderFailureCode.PAYMENT_NOT_FOUND;
                default -> OrderFailureCode.PAYMENT_FAILED;
            };
        }
        return OrderFailureCode.SYSTEM_ERROR;
    }
}
