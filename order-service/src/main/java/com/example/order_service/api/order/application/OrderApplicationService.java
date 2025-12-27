package com.example.order_service.api.order.application;

import com.example.order_service.api.common.exception.PaymentErrorCode;
import com.example.order_service.api.common.exception.PaymentException;
import com.example.order_service.api.order.application.dto.command.CreateOrderDto;
import com.example.order_service.api.order.application.dto.result.CreateOrderResponse;
import com.example.order_service.api.order.application.dto.result.OrderResponse;
import com.example.order_service.api.order.application.event.*;
import com.example.order_service.api.order.domain.model.OrderFailureCode;
import com.example.order_service.api.order.domain.model.OrderStatus;
import com.example.order_service.api.order.domain.model.vo.PriceCalculateResult;
import com.example.order_service.api.order.domain.service.OrderDomainService;
import com.example.order_service.api.order.domain.service.OrderPriceCalculator;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext;
import com.example.order_service.api.order.domain.service.dto.command.OrderItemSpec;
import com.example.order_service.api.order.domain.service.dto.result.ItemCalculationResult;
import com.example.order_service.api.order.domain.service.dto.result.OrderDto;
import com.example.order_service.api.order.infrastructure.OrderIntegrationService;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponCalcResponse;
import com.example.order_service.api.order.infrastructure.client.payment.dto.TossPaymentConfirmResponse;
import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse;
import com.example.order_service.api.order.infrastructure.client.user.dto.OrderUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderApplicationService {

    private final OrderIntegrationService orderIntegrationService;
    private final OrderPriceCalculator calculator;
    private final OrderDomainService orderDomainService;
    private final ApplicationEventPublisher eventPublisher;

    public CreateOrderResponse createOrder(CreateOrderDto dto){
        //주문 유저 조회
        OrderUserResponse user = orderIntegrationService.getOrderUser(dto.getUserPrincipal());
        //주문 상품 목록 조회
        List<OrderProductResponse> products = orderIntegrationService.getOrderProducts(dto.getOrderItemDtoList());
        //주문 상품 가격 정보 계산
        ItemCalculationResult itemResult = calculator.calculateItemAmounts(dto.getOrderItemDtoList(), products);
        OrderCouponCalcResponse coupon = orderIntegrationService.getCoupon(dto.getUserPrincipal(), dto.getCouponId(), itemResult.getSubTotalPrice());
        //할인 적용 최종 금액 계산
        PriceCalculateResult priceResult = calculator
                .calculateFinalPrice(dto.getPointToUse(), itemResult, dto.getExpectedPrice(), user, coupon);

        OrderCreationContext creationContext =
                createCreationContext(dto, user, products, priceResult);
        OrderDto orderDto = orderDomainService.saveOrder(creationContext);
        eventPublisher.publishEvent(OrderCreatedEvent.from(orderDto));
        return CreateOrderResponse.of(orderDto);
    }

    public void changePaymentWaiting(Long orderId) {
        OrderDto orderDto = orderDomainService.changeOrderStatus(orderId, OrderStatus.PAYMENT_WAITING);
        eventPublisher.publishEvent(OrderResultEvent.of(
                orderDto.getOrderId(), orderDto.getUserId(),
                OrderEventStatus.SUCCESS, OrderEventCode.PAYMENT_READY,
                orderDto.getOrderName(), orderDto.getPaymentInfo().getFinalPaymentAmount(),
                "결제 대기중입니다"
        ));
    }

    public void changeCanceled(Long orderId, OrderFailureCode orderFailureCode){
        OrderDto orderDto = orderDomainService.changeCanceled(orderId, orderFailureCode);
        eventPublisher.publishEvent(OrderResultEvent.of(
                orderDto.getOrderId(), orderDto.getUserId(),
                OrderEventStatus.FAILURE, OrderEventCode.from(orderDto.getOrderFailureCode()),
                orderDto.getOrderName(), null,
                orderDto.getOrderFailureCode().name()
        ));
    }

    public OrderResponse confirmOrder(Long orderId, String paymentKey) {
        OrderDto order = orderDomainService.getOrder(orderId);
        if (!order.getStatus().equals(OrderStatus.PAYMENT_WAITING)) {
            throw new PaymentException("주문이 결제 가능한 상태가 아닙니다", PaymentErrorCode.INVALID_STATUS);
        }

        try {
            //TODO 결제 정보 저장
            TossPaymentConfirmResponse paymentConfirm =
                    orderIntegrationService.confirmOrderPayment(order.getOrderId(), paymentKey, order.getPaymentInfo().getFinalPaymentAmount());
            OrderDto completeOrder = orderDomainService.changeOrderStatus(order.getOrderId(), OrderStatus.COMPLETED);
            eventPublisher.publishEvent(PaymentResultEvent.of(order.getOrderId(), OrderEventStatus.SUCCESS,
                    OrderEventCode.PAYMENT_AUTHORIZED, null));
            return OrderResponse.from(completeOrder);
        } catch (PaymentException e) {
            eventPublisher.publishEvent(PaymentResultEvent.of(order.getOrderId(), OrderEventStatus.FAILURE,
                    OrderEventCode.from(e.getErrorCode()) , e.getMessage()));
            throw e;
        }
    }

    private OrderCreationContext createCreationContext(CreateOrderDto dto,
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

}
