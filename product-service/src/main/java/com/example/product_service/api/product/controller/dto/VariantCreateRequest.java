package com.example.product_service.api.product.controller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VariantCreateRequest {
    @Valid
    @NotEmpty
    private List<VariantRequest> variants;

    @Getter
    public static class VariantRequest {
        private Long price;
        private Integer discountRate;
        private Integer stockQuantity;
        private List<Long> optionValueIds;

        @Builder
        private VariantRequest(Long price, Integer discountRate, Integer stockQuantity, List<Long> optionValueIds) {
            this.price = price;
            this.discountRate = discountRate;
            this.stockQuantity = stockQuantity;
            this.optionValueIds = optionValueIds;
        }
    }

    @Builder
    private VariantCreateRequest(List<VariantRequest> variants) {
        this.variants = variants;
    }
}
