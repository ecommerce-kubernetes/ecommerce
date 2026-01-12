package com.example.product_service.api.product.service.dto.command;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class AddVariantCommand {
    private Long productId;
    private List<VariantCommand> variants;

    @Builder
    private AddVariantCommand(Long productId, List<VariantCommand> variants) {
        this.productId = productId;
        this.variants = variants;
    }

    @Getter
    public static class VariantCommand {
        private Long price;
        private Integer discountRate;
        private Integer stockQuantity;
        private List<Long> optionValueIds;

        @Builder
        private VariantCommand(Long price, Integer discountRate, Integer stockQuantity, List<Long> optionValueIds) {
            this.price = price;
            this.discountRate = discountRate;
            this.stockQuantity = stockQuantity;
            this.optionValueIds = optionValueIds;
        }
    }
}
