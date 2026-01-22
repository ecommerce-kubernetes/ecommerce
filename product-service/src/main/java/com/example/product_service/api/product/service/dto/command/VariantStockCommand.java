package com.example.product_service.api.product.service.dto.command;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VariantStockCommand {
    private Long variantId;
    private Integer quantity;

    public static VariantStockCommand of(Long variantId, Integer quantity) {
        return VariantStockCommand.builder()
                .variantId(variantId)
                .quantity(quantity)
                .build();
    }
}
