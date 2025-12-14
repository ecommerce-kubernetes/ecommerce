package com.example.order_service.api.order.domain.service.dto.result;

import com.example.order_service.api.order.domain.model.Order;
import com.example.order_service.api.order.domain.model.OrderItem;
import com.example.order_service.api.order.domain.model.vo.AppliedCoupon;
import com.example.order_service.api.order.domain.model.vo.PaymentInfo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class OrderCreationResult {
    private Long orderId;
    private Long userId;
    private String status;
    private String orderName;
    private String deliveryAddress;
    private LocalDateTime orderedAt;
    private PaymentInfo paymentInfo;
    private List<OrderItemDto> orderItemDtoList;
    private AppliedCoupon appliedCoupon;

    @Builder
    private OrderCreationResult(Long orderId, Long userId, String status, String orderName, String deliveryAddress, LocalDateTime orderedAt,
                                PaymentInfo paymentInfo, List<OrderItemDto> orderItemDtoList, AppliedCoupon appliedCoupon){
        this.orderId = orderId;
        this.userId = userId;
        this.status = status;
        this.orderName = orderName;
        this.deliveryAddress = deliveryAddress;
        this.orderedAt = orderedAt;
        this.paymentInfo = paymentInfo;
        this.orderItemDtoList = orderItemDtoList;
        this.appliedCoupon = appliedCoupon;
    }

    public static OrderCreationResult from(Order order) {
        return of(
                order.getId(),
                order.getUserId(),
                order.getStatus().name(),
                order.getOrderName(),
                order.getDeliveryAddress(),
                order.getCreatedAt(),
                PaymentInfo.from(order),
                createOrderItemDto(order.getOrderItems()),
                AppliedCoupon.from(order.getCoupon())
                );
    }

    public static OrderCreationResult of(Long orderId, Long userId, String status, String orderName, String deliveryAddress, LocalDateTime orderedAt, PaymentInfo paymentInfo,
                                         List<OrderItemDto> orderItemDtoList, AppliedCoupon appliedCoupon){
        return OrderCreationResult.builder()
                .orderId(orderId)
                .userId(userId)
                .status(status)
                .orderName(orderName)
                .deliveryAddress(deliveryAddress)
                .orderedAt(orderedAt)
                .paymentInfo(paymentInfo)
                .orderItemDtoList(orderItemDtoList)
                .appliedCoupon(appliedCoupon)
                .build();
    }

    private static List<OrderItemDto> createOrderItemDto(List<OrderItem> orderItems){
        return orderItems.stream().map(OrderItemDto::from).toList();
    }
}
