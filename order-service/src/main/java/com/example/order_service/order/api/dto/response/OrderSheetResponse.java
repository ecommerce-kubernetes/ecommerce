package com.example.order_service.order.api.dto.response;

import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetResult;
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
            Orderer orderer,
            ShippingAddress shippingAddress,
            List<OrderItem> items,
            Coupon cartCoupon,
            Point point,
            PaymentSummary paymentSummary
    ) {
        public static Detail from(OrderSheetResult.Detail result) {
            return Detail.builder()
                    .sheetId(result.sheetId())
                    .expiresAt(result.expiresAt())
                    .orderer(Orderer.from(result.orderer()))
                    .shippingAddress(ShippingAddress.from(result.shippingAddress()))
                    .items(OrderItem.from(result.items()))
                    .cartCoupon(Coupon.from(result.cartCoupon()))
                    .point(Point.from(result.point()))
                    .paymentSummary(PaymentSummary.from(result.paymentSummary()))
                    .build();
        }
    }

    @Builder
    public record Orderer(
            Long userId,
            String userName,
            String phoneNumber
    ) {
        public static Orderer from(OrderSheetResult.OrdererInfo result) {
            return Orderer.builder()
                    .userId(result.userId())
                    .userName(result.userName())
                    .phoneNumber(result.phoneNumber())
                    .build();
        }
    }

    @Builder
    public record ShippingAddress(
            String receiverName,
            String receiverPhone,
            String zipCode,
            String address,
            String addressDetail
    ) {
        public static ShippingAddress from(OrderSheetResult.ShippingInfo result) {
            return ShippingAddress.builder()
                    .receiverName(result.receiverName())
                    .receiverPhone(result.receiverPhone())
                    .zipCode(result.zipCode())
                    .address(result.address())
                    .addressDetail(result.addressDetail())
                    .build();
        }
    }

    @Builder
    public record Coupon(
            Long couponId,
            String couponName,
            Long discountAmount
    ) {
        public static Coupon from(OrderSheetResult.Coupon result) {
            return Coupon.builder()
                    .couponId(result.couponId())
                    .couponName(result.couponName())
                    .discountAmount(result.discountAmount().longValue())
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
            UnitPrice unitPrice,
            Long lineTotal,
            Coupon appliedItemCoupon,
            List<ItemOption> options
    ) {
        public static OrderItem from(OrderSheetResult.OrderItem result) {
            return OrderItem.builder()
                    .sheetItemId(result.sheetItemId())
                    .productId(result.productId())
                    .productVariantId(result.productVariantId())
                    .productName(result.productName())
                    .thumbnail(result.thumbnail())
                    .quantity(result.quantity())
                    .unitPrice(UnitPrice.from(result.unitPrice()))
                    .lineTotal(result.lineTotal().longValue())
                    .appliedItemCoupon(Coupon.from(result.appliedItemCoupon()))
                    .options(ItemOption.from(result.options()))
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
    public record UnitPrice(
            Long originalPrice,
            Integer discountRate,
            Long discountAmount,
            Long discountedPrice
    ) {
        public static UnitPrice from(OrderSheetResult.OrderItemPrice result) {
            return UnitPrice.builder()
                    .originalPrice(result.originalPrice().longValue())
                    .discountRate(result.discountRate())
                    .discountAmount(result.discountAmount().longValue())
                    .discountedPrice(result.discountedPrice().longValue())
                    .build();
        }
    }

    @Builder
    public record ItemOption(
            String optionTypeName,
            String optionValueName
    ) {
        public static ItemOption from(OrderSheetResult.OrderItemOption result) {
            return ItemOption.builder()
                    .optionTypeName(result.optionTypeName())
                    .optionValueName(result.optionValueName())
                    .build();
        }

        public static List<ItemOption> from(List<OrderSheetResult.OrderItemOption> results) {
            return results.stream().map(ItemOption::from).toList();
        }
    }
}
