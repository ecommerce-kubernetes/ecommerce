package com.example.order_service.api.order.domain.service.dto.result;

import com.example.order_service.api.order.domain.model.Order;
import com.example.order_service.api.order.domain.model.OrderFailureCode;
import com.example.order_service.api.order.domain.model.OrderItem;
import com.example.order_service.api.order.domain.model.OrderStatus;
import com.example.order_service.api.order.domain.model.vo.AppliedCoupon;
import com.example.order_service.api.order.domain.model.vo.OrderPriceInfo;
import com.example.order_service.api.order.domain.model.vo.PaymentInfo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class OrderDto {
    private Long orderId;
    private Long userId;
    private OrderStatus status;
    private String orderName;
    private String deliveryAddress;
    private LocalDateTime orderedAt;
    private OrderPriceInfo orderPriceInfo;
    private List<OrderItemDto> orderItemDtoList;
    private AppliedCoupon appliedCoupon;
    private PaymentInfo paymentInfo;
    private OrderFailureCode orderFailureCode;

    @Builder
    private OrderDto(Long orderId, Long userId, OrderStatus status, String orderName, String deliveryAddress, LocalDateTime orderedAt,
                     OrderPriceInfo orderPriceInfo, List<OrderItemDto> orderItemDtoList, AppliedCoupon appliedCoupon, PaymentInfo paymentInfo, OrderFailureCode orderFailureCode){
        this.orderId = orderId;
        this.userId = userId;
        this.status = status;
        this.orderName = orderName;
        this.deliveryAddress = deliveryAddress;
        this.orderedAt = orderedAt;
        this.orderPriceInfo = orderPriceInfo;
        this.orderItemDtoList = orderItemDtoList;
        this.appliedCoupon = appliedCoupon;
        this.paymentInfo = paymentInfo;
        this.orderFailureCode = orderFailureCode;
    }

    public static OrderDto from(Order order) {
        return of(
                order.getId(),
                order.getUserId(),
                order.getStatus(),
                order.getOrderName(),
                order.getDeliveryAddress(),
                order.getCreatedAt(),
                OrderPriceInfo.from(order),
                createOrderItemDto(order.getOrderItems()),
                AppliedCoupon.from(order.getCoupon()),
                PaymentInfo.from(order.getPayment()),
                order.getFailureCode()
                );
    }

    public static OrderDto of(Long orderId, Long userId, OrderStatus status, String orderName, String deliveryAddress, LocalDateTime orderedAt, OrderPriceInfo orderPriceInfo,
                              List<OrderItemDto> orderItemDtoList, AppliedCoupon appliedCoupon, PaymentInfo paymentInfo, OrderFailureCode orderFailureCode){
        return OrderDto.builder()
                .orderId(orderId)
                .userId(userId)
                .status(status)
                .orderName(orderName)
                .deliveryAddress(deliveryAddress)
                .orderedAt(orderedAt)
                .orderPriceInfo(orderPriceInfo)
                .orderItemDtoList(orderItemDtoList)
                .appliedCoupon(appliedCoupon)
                .paymentInfo(paymentInfo)
                .orderFailureCode(orderFailureCode)
                .build();
    }

    private static List<OrderItemDto> createOrderItemDto(List<OrderItem> orderItems){
        return orderItems.stream().map(OrderItemDto::from).toList();
    }
}
