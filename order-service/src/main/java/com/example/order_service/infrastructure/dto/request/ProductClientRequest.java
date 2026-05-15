package com.example.order_service.infrastructure.dto.request;

import lombok.Builder;

import java.util.List;

public class ProductClientRequest {

    @Builder
    public record ProductVariantIds(
            List<Long> variantIds
    ) {

        public static ProductVariantIds of(List<Long> variantIds) {
            return ProductVariantIds.builder()
                    .variantIds(variantIds)
                    .build();
        }
    }

    @Builder
    public record Validate(
            List<Item> items
    ) {
    }

    @Builder
    public record Item(
            Long productVariantId,
            Integer quantity
    ) {
    }
}
