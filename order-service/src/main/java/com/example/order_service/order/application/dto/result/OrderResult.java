package com.example.order_service.order.application.dto.result;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.application.service.order.dto.result.OrderDto;
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
        public static Detail from(OrderDto.Detail dto) {
            return Detail.builder()
                    .orderNo(dto.orderNo())
                    .status(dto.status())
                    .orderName(dto.orderName())
                    .orderer(dto.orderer())
                    .shippingAddress(dto.shippingAddress())
                    .cartCoupon(dto.cartCoupon())
                    .items(OrderedItem.from(dto.orderItems()))
                    .totalOriginalPrice(dto.totalOriginalPrice())
                    .totalProductDiscountAmount(dto.totalProductDiscountAmount())
                    .totalCouponDiscountAmount(dto.totalCouponDiscountAmount())
                    .usedPoints(dto.usedPoints())
                    .totalPaymentAmount(dto.totalPaymentAmount())
                    .createdAt(dto.createdAt())
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
        public static Summary from(OrderDto.Summary dto) {
            return Summary.builder()
                    .orderNo(dto.orderNo())
                    .status(dto.status())
                    .orderName(dto.orderName())
                    .orderItems(OrderedItem.from(dto.orderItems()))
                    .createdAt(dto.createdAt())
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
        public static OrderedItem from(OrderDto.Item item) {
            return OrderedItem.builder()
                    .product(item.product())
                    .productPrice(item.productPrice())
                    .itemCoupon(item.itemCoupon())
                    .quantity(item.quantity())
                    .options(item.options())
                    .build();
        }

        public static List<OrderedItem> from(List<OrderDto.Item> items) {
            return items.stream().map(OrderedItem::from).toList();
        }
    }
}
