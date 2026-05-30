package com.example.order_service.order.application.service.order.dto.result;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.domain.model.Order;
import com.example.order_service.order.domain.model.OrderFailureCode;
import com.example.order_service.order.domain.model.OrderItem;
import com.example.order_service.order.domain.model.OrderStatus;
import com.example.order_service.order.domain.vo.*;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class OrderDto {

    @Builder
    public record Detail(
            Long id,
            String orderNo,
            OrderStatus status,
            String orderName,
            Orderer orderer,
            ShippingAddress shippingAddress,
            List<Item> orderItems,
            OrderCouponSnapshot cartCoupon,
            Money totalOriginalPrice,
            Money totalProductDiscountAmount,
            Money totalCouponDiscountAmount,
            Money usedPoints,
            Money totalPaymentAmount,
            OrderFailureCode failureCode,
            LocalDateTime createdAt
    ) {
        public static Detail from(Order order) {
            return Detail.builder()
                    .id(order.getId())
                    .orderNo(order.getOrderNo())
                    .status(order.getStatus())
                    .orderName(order.getOrderName())
                    .orderer(order.getOrderer())
                    .shippingAddress(order.getShippingAddress())
                    .orderItems(Item.from(order.getOrderItems()))
                    .cartCoupon(order.getCartCoupon())
                    .totalOriginalPrice(order.getTotalOriginalPrice())
                    .totalProductDiscountAmount(order.getTotalProductDiscountAmount())
                    .totalCouponDiscountAmount(order.getTotalCouponDiscountAmount())
                    .usedPoints(order.getUsedPoints())
                    .totalPaymentAmount(order.getTotalPaymentAmount())
                    .failureCode(order.getFailureCode())
                    .createdAt(order.getCreatedAt())
                    .build();
        }
    }

    @Builder
    public record Summary(
            String orderNo,
            OrderStatus status,
            String orderName,
            List<Item> orderItems,
            LocalDateTime createdAt
    ) {
        public static Summary from(Order order) {
            return Summary.builder()
                    .orderNo(order.getOrderNo())
                    .status(order.getStatus())
                    .orderName(order.getOrderName())
                    .orderItems(Item.from(order.getOrderItems()))
                    .createdAt(order.getCreatedAt())
                    .build();
        }
    }

    @Builder
    public record Item(
            ProductSnapshot product,
            ProductPriceSnapshot productPrice,
            OrderCouponSnapshot itemCoupon,
            Integer quantity,
            List<ProductOptionSnapshot> options
    ) {
        public static Item from(OrderItem orderItem) {
            return Item.builder()
                    .product(orderItem.getProduct())
                    .productPrice(orderItem.getProductPrice())
                    .itemCoupon(orderItem.getItemCoupon())
                    .quantity(orderItem.getQuantity())
                    .options(orderItem.getOptions())
                    .build();
        }

        public static List<Item> from(List<OrderItem> orderItems) {
            return orderItems.stream().map(Item::from).toList();
        }
    }
}
