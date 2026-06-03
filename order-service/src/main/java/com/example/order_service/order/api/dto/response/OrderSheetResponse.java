package com.example.order_service.order.api.dto.response;

import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetResult;
import com.example.order_service.order.domain.vo.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class OrderSheetResponse {

    @Builder
    public record Create(
            String sheetId,
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
            LocalDateTime expiresAt
    ) {
        public static Create from(OrderSheetResult.Create result) {
            return OrderSheetResponse.Create.builder()
                    .sheetId(result.sheetId())
                    .expiresAt(result.expiresAt())
                    .build();
        }
    }

    @Builder
    public record Detail(
            String sheetId,
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
            LocalDateTime expiresAt,
            OrdererResponse orderer,
            ShippingAddressResponse shippingAddress,
            List<OrderItem> items,
            OrderCouponResponse cartCoupon,
            Point point,
            PaymentSummary paymentSummary
    ) {
        public static Detail from(OrderSheetResult.Detail result) {
            return Detail.builder()
                    .sheetId(result.sheetId())
                    .expiresAt(result.expiresAt())
                    .orderer(OrdererResponse.from(result.orderer()))
                    .shippingAddress(ShippingAddressResponse.from(result.shippingAddress()))
                    .items(OrderItem.from(result.items()))
                    .cartCoupon(OrderCouponResponse.from(result.cartCoupon()))
                    .point(Point.from(result.point()))
                    .paymentSummary(PaymentSummary.from(result.paymentSummary()))
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
        public static OrderCouponResponse from(OrderCouponSnapshot coupon) {
            return OrderCouponResponse.builder()
                    .couponId(coupon.getCouponId())
                    .couponName(coupon.getCouponName())
                    .discountAmount(coupon.getDiscountAmount().longValue())
                    .build();
        }
    }

    @Builder
    public record Point(
            Long ownedPoints,
            Long availablePoints,
            Long usedPoints
    ) {
        public static Point from(OrderSheetResult.Point result) {
            return Point.builder()
                    .ownedPoints(result.ownedPoints().longValue())
                    .availablePoints(result.availablePoints().longValue())
                    .usedPoints(result.usedPoints().longValue())
                    .build();
        }
    }

    @Builder
    public record OrderItem(
            String sheetItemId,
            Long productId,
            Long productVariantId,
            String productName,
            String thumbnail,
            int quantity,
            ProductPriceResponse unitPrice,
            Long lineTotal,
            OrderCouponResponse appliedItemCoupon,
            List<ProductOptionResponse> options
    ) {
        public static OrderItem from(OrderSheetResult.OrderItem result) {
            return OrderItem.builder()
                    .sheetItemId(result.sheetItemId())
                    .productId(result.productId())
                    .productVariantId(result.productVariantId())
                    .productName(result.productName())
                    .thumbnail(result.thumbnail())
                    .quantity(result.quantity())
                    .unitPrice(ProductPriceResponse.from(result.productPrice()))
                    .lineTotal(result.lineTotal().longValue())
                    .appliedItemCoupon(OrderCouponResponse.from(result.appliedItemCoupon()))
                    .options(ProductOptionResponse.from(result.options()))
                    .build();
        }

        public static List<OrderItem> from(List<OrderSheetResult.OrderItem> results) {
            return results.stream().map(OrderItem::from).toList();
        }
    }

    @Builder
    public record PaymentSummary(
            Long totalOriginalPrice,
            Long totalProductDiscountAmount,
            Long totalCouponDiscount,
            Long usedPoints,
            Long totalPaymentAmount
    ) {
        public static PaymentSummary from(OrderSheetResult.PaymentSummary result) {
            return PaymentSummary.builder()
                    .totalOriginalPrice(result.totalOriginPrice().longValue())
                    .totalProductDiscountAmount(result.totalProductDiscount().longValue())
                    .totalCouponDiscount(result.totalCouponDiscount().longValue())
                    .usedPoints(result.usedPoints().longValue())
                    .totalPaymentAmount(result.totalPaymentAmount().longValue())
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
        public static ProductPriceResponse from(ProductPriceSnapshot productPrice) {
            return ProductPriceResponse.builder()
                    .originalPrice(productPrice.getOriginalPrice().longValue())
                    .discountRate(productPrice.getDiscountRate())
                    .discountAmount(productPrice.getDiscountAmount().longValue())
                    .discountedPrice(productPrice.getDiscountedPrice().longValue())
                    .build();
        }
    }

    @Builder
    public record ProductOptionResponse(
            String optionTypeName,
            String optionValueName
    ) {
        public static ProductOptionResponse from(ProductOptionSnapshot option) {
            return ProductOptionResponse.builder()
                    .optionTypeName(option.getOptionTypeName())
                    .optionValueName(option.getOptionValueName())
                    .build();
        }

        public static List<ProductOptionResponse> from(List<ProductOptionSnapshot> options) {
            return options.stream().map(ProductOptionResponse::from).toList();
        }
    }
}
