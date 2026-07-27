package com.example.order_service.order.api.web.dto.order.response;

import com.example.order_service.order.application.service.order.dto.result.OrderResult;
import com.example.order_service.order.domain.model.OrderStatus;
import com.example.order_service.order.domain.vo.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {

    @Builder
    public record Create(
            String orderNo,
            OrderStatus status,
            String orderName,
            Long totalPaymentAmount,
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
            LocalDateTime createdAt
    ) {
        public static Create from(OrderResult.Create result) {
            return Create.builder()
                    .orderNo(result.orderNo())
                    .status(result.status())
                    .orderName(result.orderName())
                    .totalPaymentAmount(result.totalPaymentAmount().longValue())
                    .createdAt(result.createdAt())
                    .build();
        }
    }

    @Builder
    public record Detail(
            String orderNo,
            OrderStatus status,
            String orderName,
            OrdererResponse orderer,
            ShippingAddressResponse shippingAddress,
            OrderCouponResponse cartCoupon,
            List<Item> orderItems,
            Long totalOriginalPrice,
            Long totalProductDiscountAmount,
            Long totalCouponDiscountAmount,
            Long usedPoints,
            Long totalPaymentAmount,
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
            LocalDateTime createdAt
    ) {
        public static Detail from(OrderResult.Detail detail) {
            return Detail.builder()
                    .orderNo(detail.orderNo())
                    .status(detail.status())
                    .orderName(detail.orderName())
                    .orderer(OrdererResponse.from(detail.orderer()))
                    .shippingAddress(ShippingAddressResponse.from(detail.shippingAddress()))
                    .cartCoupon(OrderCouponResponse.from(detail.cartCoupon()))
                    .orderItems(Item.from(detail.items()))
                    .totalOriginalPrice(detail.totalOriginalPrice().longValue())
                    .totalProductDiscountAmount(detail.totalProductDiscountAmount().longValue())
                    .totalCouponDiscountAmount(detail.totalCouponDiscountAmount().longValue())
                    .usedPoints(detail.usedPoints().longValue())
                    .totalPaymentAmount(detail.totalPaymentAmount().longValue())
                    .createdAt(detail.createdAt())
                    .build();
        }
    }

    @Builder
    public record Summary(
            String orderNo,
            OrderStatus status,
            String orderName,
            List<Item> orderItems,
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
            LocalDateTime createdAt
    ) {
        public static Summary from(OrderResult.Summary summary) {
            return Summary.builder()
                    .orderNo(summary.orderNo())
                    .status(summary.status())
                    .orderName(summary.orderName())
                    .orderItems(Item.from(summary.orderItems()))
                    .createdAt(summary.createdAt())
                    .build();
        }
    }

    @Builder
    public record OrdererResponse(
            Long userId,
            String userName,
            String phoneNumber
    ) {
        public static OrdererResponse from(Orderer orderer) {
            return OrdererResponse.builder()
                    .userId(orderer.getUserId())
                    .userName(orderer.getUserName())
                    .phoneNumber(orderer.getPhoneNumber())
                    .build();
        }
    }

    @Builder
    public record ShippingAddressResponse(
            String receiverName,
            String receiverPhone,
            String zipCode,
            String address,
            String addressDetail
    ) {
        public static ShippingAddressResponse from(ShippingAddress shippingAddress) {
            return ShippingAddressResponse.builder()
                    .receiverName(shippingAddress.getReceiverName())
                    .receiverPhone(shippingAddress.getReceiverPhone())
                    .zipCode(shippingAddress.getZipCode())
                    .address(shippingAddress.getAddress())
                    .addressDetail(shippingAddress.getAddressDetail())
                    .build();
        }
    }

    @Builder
    public record OrderCouponResponse(
            Long couponId,
            String couponName,
            Long discountAmount
    ) {
        public static OrderCouponResponse from(CartCouponSnapshot cartCouponSnapshot) {
            return OrderCouponResponse.builder()
                    .couponId(cartCouponSnapshot.getCartCouponId())
                    .couponName(cartCouponSnapshot.getName())
                    .build();
        }

        public static OrderCouponResponse from(ItemCouponSnapshot cartCouponSnapshot) {
            return OrderCouponResponse.builder()
                    .couponId(cartCouponSnapshot.getItemCouponId())
                    .couponName(cartCouponSnapshot.getName())
//                    .discountAmount(cartCouponSnapshot.getDiscountAmount().longValue())
                    .build();
        }
    }

    @Builder
    public record Item(
            ProductSnapshotResponse product,
            ProductPriceResponse price,
            OrderCouponResponse itemCoupon,
            Integer quantity,
            List<ProductOptionResponse> options
    ) {
        public static Item from(OrderResult.OrderedItem item) {
            return Item.builder()
                    .product(ProductSnapshotResponse.from(item.product()))
                    .price(ProductPriceResponse.from(item.productPrice()))
                    .itemCoupon(OrderCouponResponse.from(item.itemCoupon()))
                    .quantity(item.quantity())
                    .options(ProductOptionResponse.from(item.options()))
                    .build();
        }

        public static List<Item> from(List<OrderResult.OrderedItem> items) {
            return items.stream().map(Item::from).toList();
        }
    }

    @Builder
    public record ProductSnapshotResponse(
            Long productId,
            Long productVariantId,
            String sku,
            String productName,
            String thumbnail
    ) {
        public static ProductSnapshotResponse from(ProductSnapshot productSnapshot) {
            return ProductSnapshotResponse.builder()
                    .productId(productSnapshot.getProductId())
                    .productVariantId(productSnapshot.getProductVariantId())
                    .sku(productSnapshot.getSku())
                    .productName(productSnapshot.getProductName())
                    .thumbnail(productSnapshot.getThumbnail())
                    .build();
        }
    }

    @Builder
    public record ProductPriceResponse(
            Long originalPrice,
            Integer discountRate,
            Long discountAmount,
            Long discountedPrice
    ) {
        public static ProductPriceResponse from(ProductPriceSnapshot productPriceSnapshot) {
            return ProductPriceResponse.builder()
                    .originalPrice(productPriceSnapshot.getOriginalPrice().longValue())
                    .discountRate(productPriceSnapshot.getDiscountRate())
                    .discountAmount(productPriceSnapshot.getDiscountAmount().longValue())
                    .discountedPrice(productPriceSnapshot.getDiscountedPrice().longValue())
                    .build();
        }
    }

    @Builder
    public record ProductOptionResponse(
            String optionTypeName,
            String optionValueName
    ) {
        public static ProductOptionResponse from(ProductOptionSnapshot optionSnapshot) {
            return ProductOptionResponse.builder()
                    .optionTypeName(optionSnapshot.getOptionTypeName())
                    .optionValueName(optionSnapshot.getOptionValueName())
                    .build();
        }

        public static List<ProductOptionResponse> from(List<ProductOptionSnapshot> optionSnapshots) {
            return optionSnapshots.stream().map(ProductOptionResponse::from).toList();
        }
    }
}
