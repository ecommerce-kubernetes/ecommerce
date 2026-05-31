package com.example.order_service.order.application.dto.result;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.domain.model.Order;
import com.example.order_service.order.domain.model.OrderItem;
import com.example.order_service.order.domain.model.OrderStatus;
import com.example.order_service.order.domain.vo.*;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class OrderResult {

    @Builder
    public record Create(
            String orderNo,
            OrderStatus status,
            String orderName,
            Money totalPaymentAmount,
            LocalDateTime createdAt
    ) {
        public static Create from(Order order) {
            return Create.builder()
                    .orderNo(order.getOrderNo())
                    .status(order.getStatus())
                    .orderName(order.getOrderName())
                    .totalPaymentAmount(order.getTotalPaymentAmount())
                    .createdAt(order.getCreatedAt())
                    .build();
        }
    }

    @Builder
    public record Detail(
            String orderNo,
            OrderStatus status,
            String orderName,
            Orderer orderer,
            ShippingAddress shippingAddress,
            OrderCouponSnapshot cartCoupon,
            List<OrderedItem> items,
            Money totalOriginalPrice,
            Money totalProductDiscountAmount,
            Money totalCouponDiscountAmount,
            Money usedPoints,
            Money totalPaymentAmount,
            LocalDateTime createdAt
    ) {
        public static Detail from(Order order) {
            return Detail.builder()
                    .orderNo(order.getOrderNo())
                    .status(order.getStatus())
                    .orderName(order.getOrderName())
                    .orderer(order.getOrderer())
                    .shippingAddress(order.getShippingAddress())
                    .cartCoupon(order.getCartCoupon())
                    .items(OrderedItem.from(order.getOrderItems()))
                    .totalOriginalPrice(order.getTotalOriginalPrice())
                    .totalProductDiscountAmount(order.getTotalProductDiscountAmount())
                    .totalCouponDiscountAmount(order.getTotalCouponDiscountAmount())
                    .usedPoints(order.getUsedPoints())
                    .totalPaymentAmount(order.getTotalPaymentAmount())
                    .createdAt(order.getCreatedAt())
                    .build();
        }
    }

    @Builder
    public record Summary(
            String orderNo,
            OrderStatus status,
            String orderName,
            List<OrderedItem> orderItems,
            LocalDateTime createdAt
    ) {
        public static Summary from(Order order) {
            return Summary.builder()
                    .orderNo(order.getOrderNo())
                    .status(order.getStatus())
                    .orderName(order.getOrderName())
                    .orderItems(OrderedItem.from(order.getOrderItems()))
                    .createdAt(order.getCreatedAt())
                    .build();

        }
    }

    @Builder
    public record OrderedItem(
            ProductSnapshot product,
            ProductPriceSnapshot productPrice,
            OrderCouponSnapshot itemCoupon,
            Integer quantity,
            List<ProductOptionSnapshot> options
    ) {
        public static OrderedItem from(OrderItem item) {
            return OrderedItem.builder()
                    .product(item.getProduct())
                    .productPrice(item.getProductPrice())
                    .itemCoupon(item.getItemCoupon())
                    .quantity(item.getQuantity())
                    .options(item.getOptions())
                    .build();
        }

        public static List<OrderedItem> from(List<OrderItem> items) {
            return items.stream().map(OrderedItem::from).toList();
        }
    }
}
