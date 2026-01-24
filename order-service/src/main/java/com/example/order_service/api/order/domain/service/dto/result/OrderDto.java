package com.example.order_service.api.order.domain.service.dto.result;

import com.example.order_service.api.order.domain.model.Order;
import com.example.order_service.api.order.domain.model.OrderFailureCode;
import com.example.order_service.api.order.domain.model.OrderItem;
import com.example.order_service.api.order.domain.model.OrderStatus;
import com.example.order_service.api.order.domain.model.vo.CouponInfo;
import com.example.order_service.api.order.domain.model.vo.OrderPriceDetail;
import com.example.order_service.api.order.domain.model.vo.Orderer;
import com.example.order_service.api.order.domain.model.vo.PaymentInfo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class OrderDto {
    private Long id;
    private String orderNo;
    private OrderStatus status;
    private String orderName;
    private Orderer orderer;
    private OrderPriceDetail orderPriceDetail;
    private CouponInfo couponInfo;
    private List<OrderItemDto> orderItems;
    private String deliveryAddress;
    private PaymentInfo paymentInfo;
    private LocalDateTime orderedAt;
    private OrderFailureCode orderFailureCode;

    public static OrderDto from(Order order) {
        return OrderDto.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .status(order.getStatus())
                .orderName(order.getOrderName())
                .orderer(order.getOrderer())
                .orderPriceDetail(order.getOrderPriceDetail())
                .couponInfo(order.getCoupon().getCouponInfo())
                .orderItems(createOrderItemDto(order.getOrderItems()))
                .deliveryAddress(order.getDeliveryAddress())
                .paymentInfo(PaymentInfo.from(order.getPayment()))
                .orderedAt(order.getCreatedAt())
                .orderFailureCode(order.getFailureCode())
                .build();
    }

    private static List<OrderItemDto> createOrderItemDto(List<OrderItem> orderItems){
        return orderItems.stream().map(OrderItemDto::from).toList();
    }
}
