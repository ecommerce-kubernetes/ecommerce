package com.example.order_service.order.application.service.order.dto.result;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.domain.order.Order;
import com.example.order_service.order.domain.order.OrderItem;
import com.example.order_service.order.domain.order.OrderStatus;
import com.example.order_service.order.domain.ordersheet.CartCouponSnapshot;
import com.example.order_service.order.domain.ordersheet.ItemCouponSnapshot;
import com.example.order_service.order.domain.vo.*;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Deprecated
public class OrderResultDeprecated {

    @Builder
    public record Create(
            Long orderId,
            String orderNo,
            OrderStatus status,
            String orderName,
            Money totalPaymentAmount,
            LocalDateTime createdAt
    ) {
        public static Create from(Order order) {
            return Create.builder()
                    .status(order.getStatus())
                    .orderName(order.getOrderName())
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
            CartCouponSnapshot cartCoupon,
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
                    .status(order.getStatus())
                    .orderName(order.getOrderName())
                    .orderer(order.getOrderer())
                    .shippingAddress(order.getShippingAddress())
//                    .cartCoupon(order.getCartCoupon())
                    .items(OrderedItem.from(order.getOrderItems()))
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
            ItemCouponSnapshot itemCoupon,
            Integer quantity,
            List<ProductOptionSnapshot> options
    ) {
        public static OrderedItem from(OrderItem item) {
            return OrderedItem.builder()
                    .product(item.getProduct())
                    .productPrice(item.getProductPrice())
//                    .itemCoupon(item.getItemCoupon())
                    .quantity(item.getQuantity())
                    .options(item.getOptions())
                    .build();
        }

        public static List<OrderedItem> from(List<OrderItem> items) {
            return items.stream().map(OrderedItem::from).toList();
        }
    }
}
