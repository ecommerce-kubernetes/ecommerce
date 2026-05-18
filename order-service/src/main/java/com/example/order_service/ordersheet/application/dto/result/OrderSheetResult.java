package com.example.order_service.ordersheet.application.dto.result;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.ordersheet.domain.model.OrderSheet;
import com.example.order_service.ordersheet.domain.model.OrderSheetItem;
import com.example.order_service.ordersheet.domain.model.vo.*;
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
                    .sheetId(orderSheet.getSheetId())
                    .expiresAt(orderSheet.getExpiresAt())
                    .build();
        }
    }

    @Builder
    public record Detail(
            String sheetId,
            LocalDateTime expiresAt,
            OrdererInfo orderer,
            ShippingInfo shippingAddress,
            List<OrderItem> items,
            Coupon cartCoupon,
            Point point,
            PaymentSummary paymentSummary
    ) {
        public static Detail of(OrderSheet orderSheet, Money availablePoints) {
            return Detail.builder()
                    .sheetId(orderSheet.getSheetId())
                    .expiresAt(orderSheet.getExpiresAt())
                    .orderer(OrdererInfo.from(orderSheet.getOrderer()))
                    .shippingAddress(ShippingInfo.from(orderSheet.getShippingAddress()))
                    .items(OrderItem.from(orderSheet.getItems()))
                    .cartCoupon(Coupon.from(orderSheet.getCartCoupon()))
                    .point(Point.of(orderSheet, availablePoints))
                    .paymentSummary(PaymentSummary.from(orderSheet))
                    .build();
        }
    }

    @Builder
    public record OrdererInfo(
            Long userId,
            String userName,
            String phoneNumber
    ) {
        public static OrdererInfo from(Orderer orderer) {
            return OrdererInfo.builder()
                    .userId(orderer.getUserId())
                    .userName(orderer.getUserName())
                    .phoneNumber(orderer.getPhoneNumber())
                    .build();
        }
    }

    @Builder
    public record Point(
            Money availablePoints,
            Money usedPoints
    ) {
        public static Point of(OrderSheet orderSheet, Money availablePoints) {
            return Point.builder()
                    .availablePoints(availablePoints)
                    .usedPoints(orderSheet.getUsedPoints())
                    .build();
        }
    }

    @Builder
    public record Coupon(
            Long couponId,
            String couponName,
            Money discountAmount
    ) {
        public static Coupon from(OrderCouponSnapshot coupon) {
            return Coupon.builder()
                    .couponId(coupon.getCouponId())
                    .couponName(coupon.getCouponName())
                    .discountAmount(coupon.getDiscountAmount())
                    .build();
        }
    }

    @Builder
    public record ShippingInfo(
            String receiverName,
            String receiverPhone,
            String zipCode,
            String address,
            String addressDetail
    ) {
        public static ShippingInfo from (ShippingAddress shippingAddress) {
            return ShippingInfo.builder()
                    .receiverName(shippingAddress.getReceiverName())
                    .receiverPhone(shippingAddress.getReceiverPhone())
                    .zipCode(shippingAddress.getZipCode())
                    .address(shippingAddress.getAddress())
                    .addressDetail(shippingAddress.getAddressDetail())
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
            OrderItemPrice unitPrice,
            Money lineTotal,
            Coupon appliedItemCoupon,
            List<OrderItemOption> options
    ) {
        public static OrderItem from(OrderSheetItem item) {
            return OrderItem.builder()
                    .sheetItemId(item.getSheetItemId())
                    .productId(item.getProductSnapshot().getProductId())
                    .productVariantId(item.getProductSnapshot().getProductVariantId())
                    .productName(item.getProductSnapshot().getProductName())
                    .thumbnail(item.getProductSnapshot().getThumbnail())
                    .quantity(item.getQuantity())
                    .unitPrice(OrderItemPrice.from(item.getItemPrice()))
                    .lineTotal(item.getFinalLineTotal())
                    .appliedItemCoupon(Coupon.from(item.getItemCoupon()))
                    .options(OrderItemOption.from(item.getOptions()))
                    .build();
        }

        public static List<OrderItem> from (List<OrderSheetItem> items) {
            return items.stream().map(OrderItem::from).toList();
        }
    }

    @Builder
    public record OrderItemPrice(
            Money originalPrice,
            int discountRate,
            Money discountAmount,
            Money discountedPrice
    ) {
        public static OrderItemPrice from(OrderSheetItemPriceSnapshot price) {
            return OrderItemPrice.builder()
                    .originalPrice(price.getOriginalPrice())
                    .discountRate(price.getDiscountRate())
                    .discountAmount(price.getDiscountAmount())
                    .discountedPrice(price.getDiscountedPrice())
                    .build();
        }
    }

    @Builder
    public record OrderItemOption(
            String optionTypeName,
            String optionValueName
    ) {
        public static OrderItemOption from(OrderSheetItemOptionSnapshot option) {
            return OrderItemOption.builder()
                    .optionTypeName(option.getOptionTypeName())
                    .optionValueName(option.getOptionValueName())
                    .build();
        }

        public static List<OrderItemOption> from(List<OrderSheetItemOptionSnapshot> options) {
            return options.stream().map(OrderItemOption::from).toList();
        }
    }
}
