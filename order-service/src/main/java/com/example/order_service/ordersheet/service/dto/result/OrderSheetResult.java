package com.example.order_service.ordersheet.service.dto.result;

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
    }

    @Builder
    public record Summary(
            long totalOriginPrice,
            long totalProductDiscount,
            long totalBasePaymentAmount
    ) {
    }

    @Builder
    public record OrderItem(
            Long productId,
            Long productVariantId,
            String productName,
            String thumbnail,
            int quantity,
            OrderItemPrice unitPrice,
            long lineTotal,
            List<OrderItemOption> options
    ) {
    }

    @Builder
    public record OrderItemPrice(
            long originalPrice,
            long discountRate,
            long discountAmount,
            long discountedPrice
    ) {
    }

    @Builder
    public record OrderItemOption(
            String optionTypeName,
            String optionValueName
    ) {

    }
}
