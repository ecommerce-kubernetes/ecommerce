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
    private String orderNo;
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
    private OrderDto(Long orderId, String orderNo, Long userId, OrderStatus status, String orderName, String deliveryAddress, LocalDateTime orderedAt,
                     OrderPriceInfo orderPriceInfo, List<OrderItemDto> orderItemDtoList, AppliedCoupon appliedCoupon, PaymentInfo paymentInfo, OrderFailureCode orderFailureCode){
        this.orderId = orderId;
        this.orderNo = orderNo;
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
        return OrderDto.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .userId(order.getUserId())
                .status(order.getStatus())
                .orderName(order.getOrderName())
                .deliveryAddress(order.getDeliveryAddress())
                .orderedAt(order.getCreatedAt())
                .orderPriceInfo(order.getPriceInfo())
                .orderItemDtoList(createOrderItemDto(order.getOrderItems()))
                .appliedCoupon(AppliedCoupon.from(order.getCoupon()))
                .paymentInfo(PaymentInfo.from(order.getPayment()))
                .orderFailureCode(order.getFailureCode())
                .build();
    }

    private static List<OrderItemDto> createOrderItemDto(List<OrderItem> orderItems){
        return orderItems.stream().map(OrderItemDto::from).toList();
    }
}
