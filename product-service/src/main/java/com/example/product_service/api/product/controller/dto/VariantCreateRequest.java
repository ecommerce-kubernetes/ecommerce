package com.example.product_service.api.product.controller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VariantCreateRequest {
    @Valid
    @NotEmpty
    private List<VariantRequest> variants;

    public static class VariantRequest {
        private String sku;
        private Long price;
        private Integer discountRate;
        private Integer stockQuantity;
        private List<Long> optionValueIds;
    }
}
