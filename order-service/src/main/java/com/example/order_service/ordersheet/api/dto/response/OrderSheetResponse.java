package com.example.order_service.ordersheet.api.dto.response;

import com.example.order_service.ordersheet.application.dto.result.OrderSheetResult;
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
            List<OrderItem> items,
            PaymentSummary paymentSummary,
            UserAssets userAssets
    ) {
        public static Detail from(OrderSheetResult.Detail result) {
            return Detail.builder()
                    .sheetId(result.sheetId())
                    .expiresAt(result.expiresAt())
                    .items(OrderItem.from(result.items()))
                    .paymentSummary(PaymentSummary.from(result.paymentSummary()))
                    .userAssets(UserAssets.from(result.userAssets()))
                    .build();
        }
    }

    @Builder
    public record OrderItem(
            Long productId,
            Long productVariantId,
            String productName,
            String thumbnail,
            int quantity,
            UnitPrice unitPrice,
            Long lineTotal,
            List<ItemOption> options
    ) {
        public static OrderItem from(OrderSheetResult.OrderItem result) {
            return OrderItem.builder()
                    .productId(result.productId())
                    .productVariantId(result.productVariantId())
                    .productName(result.productName())
                    .thumbnail(result.thumbnail())
                    .quantity(result.quantity())
                    .unitPrice(UnitPrice.from(result.unitPrice()))
                    .lineTotal(result.lineTotal().longValue())
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
            Long totalBasePaymentAmount
    ) {
        public static PaymentSummary from(OrderSheetResult.Summary result) {
            return PaymentSummary.builder()
                    .totalOriginalPrice(result.totalOriginPrice().longValue())
                    .totalProductDiscountAmount(result.totalProductDiscount().longValue())
                    .totalBasePaymentAmount(result.totalBasePaymentAmount().longValue())
                    .build();
        }
    }

    @Builder
    public record UserAssets(
            Long availablePoint,
            List<AvailableCoupon> coupons
    ) {
        public static UserAssets from(OrderSheetResult.UserAssets result) {
            return UserAssets.builder()
                    .availablePoint(result.availablePoint().longValue())
                    .coupons(AvailableCoupon.from(result.coupons()))
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
    public record ItemOption (
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

    @Builder
    public record AvailableCoupon(
            Long couponId,
            String couponName,
            Long discountAmount,
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
            LocalDateTime expiresAt
    ) {
        public static AvailableCoupon from (OrderSheetResult.AvailableCoupon result) {
            return AvailableCoupon.builder()
                    .couponId(result.couponId())
                    .couponName(result.couponName())
                    .discountAmount(result.discountAmount().longValue())
                    .expiresAt(result.expiresAt())
                    .build();
        }

        public static List<AvailableCoupon> from(List<OrderSheetResult.AvailableCoupon> results) {
            return results.stream().map(AvailableCoupon::from).toList();
        }
    }
}
