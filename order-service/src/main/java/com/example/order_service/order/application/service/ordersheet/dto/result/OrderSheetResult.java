package com.example.order_service.order.application.service.ordersheet.dto.result;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.domain.model.OrderSheet;
import com.example.order_service.order.domain.model.OrderSheetItem;
import com.example.order_service.order.domain.vo.*;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class OrderSheetResult {

    @Builder
    public record Create(
            String sheetId,
            LocalDateTime expiresAt
    ) {
        public static Create from(OrderSheet orderSheet) {
            return Create.builder()
                    .sheetId(orderSheet.getId())
                    .expiresAt(orderSheet.getExpiresAt())
                    .build();
        }
    }

    @Builder
    public record Detail(
            String sheetId,
            LocalDateTime expiresAt,
            Orderer orderer,
            ShippingAddress shippingAddress,
            List<OrderItem> items,
            OrderCouponSnapshot cartCoupon,
            Point point,
            PaymentSummary paymentSummary
    ) {
        public static Detail of(OrderSheet orderSheet, Money ownedPoints, Money availablePoints) {
            return Detail.builder()
                    .sheetId(orderSheet.getId())
                    .expiresAt(orderSheet.getExpiresAt())
                    .orderer(orderSheet.getOrderer())
                    .shippingAddress(orderSheet.getShippingAddress())
                    .items(OrderItem.from(orderSheet.getItems()))
                    .cartCoupon(orderSheet.getCartCoupon())
                    .point(Point.of(orderSheet, ownedPoints, availablePoints))
                    .paymentSummary(PaymentSummary.from(orderSheet))
                    .build();
        }
    }

    @Builder
    public record Point(
            Money ownedPoints,
            Money availablePoints,
            Money usedPoints
    ) {
        public static Point of(OrderSheet orderSheet, Money ownedPoints, Money availablePoints) {
            return Point.builder()
                    .ownedPoints(ownedPoints)
                    .availablePoints(availablePoints)
                    .usedPoints(orderSheet.getUsedPoints())
                    .build();
        }
    }

    @Builder
    public record PaymentSummary(
            Money totalOriginPrice,
            Money totalProductDiscount,
            Money totalCouponDiscount,
            Money usedPoints,
            Money totalPaymentAmount
    ) {
        public static PaymentSummary from(OrderSheet orderSheet) {
            return PaymentSummary.builder()
                    .totalOriginPrice(orderSheet.getTotalOriginalPrice())
                    .totalProductDiscount(orderSheet.getTotalProductDiscountAmount())
                    .totalCouponDiscount(orderSheet.getTotalCouponDiscountAmount())
                    .usedPoints(orderSheet.getUsedPoints())
                    .totalPaymentAmount(orderSheet.getTotalPaymentAmount())
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
            ProductPriceSnapshot productPrice,
            Money lineTotal,
            OrderCouponSnapshot appliedItemCoupon,
            List<ProductOptionSnapshot> options
    ) {
        public static OrderItem from(OrderSheetItem item) {
            return OrderItem.builder()
                    .sheetItemId(item.getId())
                    .productId(item.getProductSnapshot().getProductId())
                    .productVariantId(item.getProductSnapshot().getProductVariantId())
                    .productName(item.getProductSnapshot().getProductName())
                    .thumbnail(item.getProductSnapshot().getThumbnail())
                    .quantity(item.getQuantity())
                    .productPrice(item.getPriceSnapshot())
                    .lineTotal(item.getFinalLineTotal())
                    .appliedItemCoupon(item.getItemCouponSnapshot())
                    .options(item.getOptionSnapshots())
                    .build();
        }

        public static List<OrderItem> from (List<OrderSheetItem> items) {
            return items.stream().map(OrderItem::from).toList();
        }
    }
}
