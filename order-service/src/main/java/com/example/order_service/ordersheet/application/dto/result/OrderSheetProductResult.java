package com.example.order_service.ordersheet.application.dto.result;

import lombok.Builder;

import java.util.List;

public class OrderSheetProductResult {

    @Builder
    public record Info(
            Long productId,
            Long productVariantId,
            String sku,
            String productName,
            Long originalPrice,
            Integer discountRate,
            Long discountAmount,
            Long discountedPrice,
            String thumbnail,
            List<Option> options
    ) {
    }

    @Builder
    public record Option(
            String optionTypeName,
            String optionValueName
    ) {
    }
}
