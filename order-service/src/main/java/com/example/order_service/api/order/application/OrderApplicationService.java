package com.example.order_service.api.order.application;

import com.example.order_service.api.common.dto.PageDto;
import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.OrderErrorCode;
import com.example.order_service.api.common.exception.PaymentException;
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
import com.example.order_service.api.order.domain.service.dto.command.PaymentCreationCommand;
import com.example.order_service.api.order.domain.service.dto.result.ItemCalculationResult;
import com.example.order_service.api.order.domain.service.dto.result.OrderDto;
import com.example.order_service.api.order.domain.service.dto.result.OrderItemDto;
import com.example.order_service.api.order.infrastructure.OrderExternalAdaptor;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponDiscountResponse;
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

    public void preparePayment(Long orderId) {
        OrderDto orderDto = orderDomainService.changeOrderStatus(orderId, OrderStatus.PAYMENT_WAITING);
        eventPublisher.publishEvent(OrderResultEvent.of(
                orderDto.getOrderId(), orderDto.getUserId(),
                OrderEventStatus.SUCCESS, OrderEventCode.PAYMENT_READY,
                orderDto.getOrderName(), orderDto.getOrderPriceInfo().getFinalPaymentAmount(),
                "결제 대기중입니다"
        ));
    }

    public void processOrderFailure(Long orderId, OrderFailureCode orderFailureCode){
        OrderDto orderDto = orderDomainService.canceledOrder(orderId, orderFailureCode);
        eventPublisher.publishEvent(OrderResultEvent.of(
                orderDto.getOrderId(), orderDto.getUserId(),
                OrderEventStatus.FAILURE, OrderEventCode.from(orderDto.getOrderFailureCode()),
                orderDto.getOrderName(), null,
                orderDto.getOrderFailureCode().name()
        ));
    }

    public OrderDetailResponse finalizeOrder(Long orderId, Long userId, String paymentKey) {
        OrderDto order = orderDomainService.getOrder(orderId, userId);
        if (!order.getStatus().equals(OrderStatus.PAYMENT_WAITING)) {
            throw new BusinessException(OrderErrorCode.ORDER_NOT_PAYABLE);
        }

        try {
            TossPaymentConfirmResponse paymentConfirm =
                    orderExternalAdaptor.confirmOrderPayment(order.getOrderId(), paymentKey, order.getOrderPriceInfo().getFinalPaymentAmount());
            OrderDto completeOrder = orderDomainService.completedOrder(PaymentCreationCommand.from(paymentConfirm));
            List<Long> productVariantIds = completeOrder.getOrderItemDtoList().stream().map(OrderItemDto::getProductVariantId).toList();
            eventPublisher.publishEvent(PaymentResultEvent.of(completeOrder.getOrderId(), completeOrder.getUserId(), OrderEventStatus.SUCCESS,
                    OrderEventCode.PAYMENT_AUTHORIZED, productVariantIds, null));
            return OrderDetailResponse.from(completeOrder);
        } catch (PaymentException e) {
            OrderDto canceledOrder = orderDomainService.canceledOrder(order.getOrderId(), OrderFailureCode.from(e.getErrorCode()));
            eventPublisher.publishEvent(PaymentResultEvent.of(canceledOrder.getOrderId(), canceledOrder.getUserId(), OrderEventStatus.FAILURE,
                    OrderEventCode.from(canceledOrder.getOrderFailureCode()), null, e.getMessage()));
            throw e;
        }
    }

    public OrderDetailResponse getOrder(Long userId, Long orderId) {
        OrderDto order = orderDomainService.getOrder(orderId, userId);
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

}
