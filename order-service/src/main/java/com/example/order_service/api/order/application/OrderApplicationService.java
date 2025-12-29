package com.example.order_service.api.order.application;

import com.example.order_service.api.common.dto.PageDto;
import com.example.order_service.api.common.exception.NoPermissionException;
import com.example.order_service.api.common.exception.OrderVerificationException;
import com.example.order_service.api.common.exception.PaymentException;
import com.example.order_service.api.common.security.principal.UserPrincipal;
import com.example.order_service.api.order.application.dto.command.CreateOrderDto;
import com.example.order_service.api.order.application.dto.result.CreateOrderResponse;
import com.example.order_service.api.order.application.dto.result.OrderDetailResponse;
import com.example.order_service.api.order.application.dto.result.OrderListResponse;
import com.example.order_service.api.order.application.event.*;
import com.example.order_service.api.order.controller.dto.request.OrderSearchCondition;
import com.example.order_service.api.order.domain.model.OrderFailureCode;
import com.example.order_service.api.order.domain.model.OrderStatus;
import com.example.order_service.api.order.domain.model.vo.PriceCalculateResult;
import com.example.order_service.api.order.domain.service.OrderDomainService;
import com.example.order_service.api.order.domain.service.OrderPriceCalculator;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext;
import com.example.order_service.api.order.domain.service.dto.command.OrderItemSpec;
import com.example.order_service.api.order.domain.service.dto.result.ItemCalculationResult;
import com.example.order_service.api.order.domain.service.dto.result.OrderDto;
import com.example.order_service.api.order.domain.service.dto.result.OrderItemDto;
import com.example.order_service.api.order.infrastructure.OrderIntegrationService;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponCalcResponse;
import com.example.order_service.api.order.infrastructure.client.payment.dto.TossPaymentConfirmResponse;
import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse;
import com.example.order_service.api.order.infrastructure.client.user.dto.OrderUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
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

    public OrderDetailResponse confirmOrder(Long orderId, String paymentKey) {
        OrderDto order = orderDomainService.getOrder(orderId);
        if (!order.getStatus().equals(OrderStatus.PAYMENT_WAITING)) {
            throw new OrderVerificationException("결제 가능한 주문이 아닙니다");
        }

        try {
            //TODO 결제 정보 저장
            TossPaymentConfirmResponse paymentConfirm =
                    orderIntegrationService.confirmOrderPayment(order.getOrderId(), paymentKey, order.getPaymentInfo().getFinalPaymentAmount());
            OrderDto completeOrder = orderDomainService.changeOrderStatus(order.getOrderId(), OrderStatus.COMPLETED);
            List<Long> productVariantIds = completeOrder.getOrderItemDtoList().stream().map(OrderItemDto::getProductVariantId).toList();
            eventPublisher.publishEvent(PaymentResultEvent.of(completeOrder.getOrderId(), completeOrder.getUserId(), OrderEventStatus.SUCCESS,
                    OrderEventCode.PAYMENT_AUTHORIZED, productVariantIds, null));
            return OrderDetailResponse.from(completeOrder);
        } catch (PaymentException e) {
            OrderDto canceledOrder = orderDomainService.changeCanceled(order.getOrderId(), OrderFailureCode.from(e.getErrorCode()));
            eventPublisher.publishEvent(PaymentResultEvent.of(canceledOrder.getOrderId(), canceledOrder.getUserId(), OrderEventStatus.FAILURE,
                    OrderEventCode.from(canceledOrder.getOrderFailureCode()), null, e.getMessage()));
            throw e;
        }
    }

    public OrderDetailResponse getOrder(UserPrincipal userPrincipal, Long orderId) {
        Long userId = userPrincipal.getUserId();
        OrderDto order = orderDomainService.getOrder(orderId);
        if (!userId.equals(order.getUserId())) {
            throw new NoPermissionException("주문을 조회할 권한이 없습니다");
        }
        return OrderDetailResponse.from(order);
    }

    public PageDto<OrderListResponse> getOrders(UserPrincipal userPrincipal, OrderSearchCondition condition){
        Page<OrderDto> orders = orderDomainService.getOrders(userPrincipal.getUserId(), condition);

        List<OrderListResponse> content = orders.getContent().stream().map(OrderListResponse::from).toList();
        return PageDto.of(orders, content);
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
