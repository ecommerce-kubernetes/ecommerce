package com.example.order_service.ordersheet.application.dto.result;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.ordersheet.domain.model.OrderSheet;
import com.example.order_service.ordersheet.domain.model.OrderSheetItem;
import com.example.order_service.ordersheet.domain.model.vo.OrderSheetItemOptionSnapshot;
import com.example.order_service.ordersheet.domain.model.vo.OrderSheetItemPriceSnapshot;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class OrderSheetResult {

    @Builder
    public record Default(
            String sheetId,
            LocalDateTime expiresAt,
            Summary summary,
            List<OrderItem> items
    ) {
        public static Default from(OrderSheet orderSheet) {
            return Default.builder()
                    .sheetId(orderSheet.getSheetId())
                    .expiresAt(orderSheet.getExpiresAt())
                    .summary(Summary.of(orderSheet.getTotalOriginalPrice(), orderSheet.getTotalProductDiscountAmount(), orderSheet.getTotalPaymentAmount()))
                    .items(mapToOrderItems(orderSheet.getItems()))
                    .build();
        }

        private static List<OrderItem> mapToOrderItems(List<OrderSheetItem> items) {
            return items.stream().map(OrderItem::from).toList();
        }
    }

    @Builder
    public record Detail(
            String sheetId,
            LocalDateTime expiresAt,
            List<OrderItem> items,
            Summary paymentSummary,
            UserAssets userAssets
    ) {
    }

    @Builder
    public record Summary(
            Money totalOriginPrice,
            Money totalProductDiscount,
            Money totalBasePaymentAmount
    ) {
        public static Summary of(Money totalOriginalPrice, Money totalProductDiscount, Money totalBasePaymentAmount) {
            return Summary.builder()
                    .totalOriginPrice(totalOriginalPrice)
                    .totalProductDiscount(totalProductDiscount)
                    .totalBasePaymentAmount(totalBasePaymentAmount)
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
            OrderItemPrice unitPrice,
            Money lineTotal,
            List<OrderItemOption> options
    ) {
        public static OrderItem from(OrderSheetItem item) {
            return OrderItem.builder()
                    .productId(item.getProductSnapshot().getProductId())
                    .productVariantId(item.getProductSnapshot().getProductVariantId())
                    .productName(item.getProductSnapshot().getProductName())
                    .thumbnail(item.getProductSnapshot().getThumbnail())
                    .quantity(item.getQuantity())
                    .unitPrice(OrderItemPrice.from(item.getItemPrice()))
                    .lineTotal(item.getLineTotal())
                    .options(mapToOptions(item.getOptions()))
                    .build();

        }
        private static List<OrderItemOption> mapToOptions(List<OrderSheetItemOptionSnapshot> options) {
            return options.stream().map(OrderItemOption::from)
                    .toList();
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
    }

    @Builder
    public record UserAssets(
            Money availablePoint,
            List<AvailableCoupon> coupons
    ) {}

    @Builder
    public record AvailableCoupon (
            Long couponId,
            String couponName,
            Money discountAmount,
            LocalDateTime expiresAt
    ) {
    }
}
