package com.example.order_service.order.application.service.ordersheet.dto.result;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.domain.model.OrderSheet;
import com.example.order_service.order.domain.model.OrderSheetItem;
import com.example.order_service.order.domain.vo.Orderer;
import com.example.order_service.order.domain.vo.ProductOptionSnapshot;
import com.example.order_service.order.domain.vo.ProductSnapshot;
import com.example.order_service.order.domain.vo.ShippingAddress;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderSheetResult(
        String orderSheetId,
        Orderer orderer,
        ShippingAddress shippingAddress,
        List<OrderSheetItemResult> items,
        AppliedCartCouponResult cartCoupon,
        PaymentSummaryResult paymentSummary,
        PointResult point,

        LocalDateTime expiresAt
) {

    public static OrderSheetResult of(OrderSheet orderSheet, Money availablePoints, Money maxUsablePoints) {
        return OrderSheetResult.builder()
                .orderSheetId(orderSheet.getId())
                .orderer(orderSheet.getOrderer())
                .shippingAddress(orderSheet.getShippingAddress())
                .items(OrderSheetItemResult.from(orderSheet.getItems()))
                .cartCoupon(AppliedCartCouponResult.from(orderSheet))
                .paymentSummary(PaymentSummaryResult.from(orderSheet))
                .point(PointResult.of(availablePoints, maxUsablePoints))
                .expiresAt(orderSheet.getExpiresAt())
                .build();
    }

    @Builder
    public record OrderSheetItemResult(
            String orderSheetItemId,
            int quantity,
            ProductSnapshot product,
            List<ProductOptionSnapshot> options,
            ItemPriceResult price,
            AppliedItemCouponResult coupon
    ) {
        public static OrderSheetItemResult from(OrderSheetItem orderSheetItem) {
            return OrderSheetItemResult.builder()
                    .orderSheetItemId(orderSheetItem.getId())
                    .quantity(orderSheetItem.getQuantity())
                    .product(orderSheetItem.getProductSnapshot())
                    .options(orderSheetItem.getOptionSnapshots())
                    .price(ItemPriceResult.from(orderSheetItem))
                    .coupon(AppliedItemCouponResult.from(orderSheetItem))
                    .build();
        }

        public static List<OrderSheetItemResult> from(List<OrderSheetItem> orderSheetItems) {
            return orderSheetItems.stream().map(OrderSheetItemResult::from).toList();
        }
    }

    @Builder
    public record AppliedCartCouponResult(
            Long cartCouponId,
            String name,
            Money appliedDiscountAmount
    ) {
        public static AppliedCartCouponResult from(OrderSheet orderSheet) {
            return AppliedCartCouponResult.builder()
                    .cartCouponId(orderSheet.getCartCoupon().getCartCouponId())
                    .name(orderSheet.getCartCoupon().getName())
                    .appliedDiscountAmount(orderSheet.calculateCartCouponDiscount())
                    .build();
        }
    }

    @Builder
    public record PaymentSummaryResult(
            Money totalOriginalAmount,
            Money totalItemDiscount,
            Money totalItemCouponDiscount,
            Money cartCouponDiscount,
            Money usedPoints,
            Money totalPaymentAmount
    ) {

        public static PaymentSummaryResult from(OrderSheet orderSheet) {
            return PaymentSummaryResult.builder()
                    .totalOriginalAmount(orderSheet.calculateTotalOriginalAmount())
                    .totalItemDiscount(orderSheet.calculateTotalItemDiscount())
                    .totalItemCouponDiscount(orderSheet.calculateTotalItemCouponDiscount())
                    .cartCouponDiscount(orderSheet.calculateCartCouponDiscount())
                    .usedPoints(orderSheet.getUsedPoints())
                    .totalPaymentAmount(orderSheet.calculateTotalPaymentAmount())
                    .build();
        }
    }

    @Builder
    public record ItemPriceResult(
            Money unitOriginalPrice,
            Money unitDiscountedPrice,
            Money lineTotal,
            Money finalAmount
    ) {
        public static ItemPriceResult from(OrderSheetItem orderSheetItem) {
            return ItemPriceResult.builder()
                    .unitOriginalPrice(orderSheetItem.getPriceSnapshot().getOriginalPrice())
                    .unitDiscountedPrice(orderSheetItem.getPriceSnapshot().getDiscountedPrice())
                    .lineTotal(orderSheetItem.calculateLineTotal())
                    .finalAmount(orderSheetItem.calculateFinalAmount())
                    .build();
        }
    }

    @Builder
    public record AppliedItemCouponResult(
            Long itemCouponId,
            String name,
            Money appliedDiscountAmount
    ) {
        public static AppliedItemCouponResult from(OrderSheetItem orderSheetItem) {
            return AppliedItemCouponResult.builder()
                    .itemCouponId(orderSheetItem.getItemCouponSnapshot().getItemCouponId())
                    .name(orderSheetItem.getItemCouponSnapshot().getName())
                    .appliedDiscountAmount(orderSheetItem.calculateCouponDiscount())
                    .build();
        }
    }

    @Builder
    public record PointResult(
            Money availablePoints,
            Money maxUsablePoints
    ) {
        public static PointResult of(Money availablePoints, Money maxUsablePoints) {
            return PointResult.builder()
                    .availablePoints(availablePoints)
                    .maxUsablePoints(maxUsablePoints)
                    .build();
        }
    }
}
