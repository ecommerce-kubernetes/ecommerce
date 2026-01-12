package com.example.product_service.api.product.service.dto.result;

import lombok.Getter;

import java.util.List;

@Getter
public class VariantCreateResponse {
    private Long productId;
    private List<CreatedVariantResponse> variants;

    public static class CreatedVariantResponse {
        private Long productVariantId;
        private String sku;
        private List<VariantOptionResponse> options;
        private Long originalPrice;
        private Long discountedPrice;
        private Integer discountRate;
        private Integer stockQuantity;
    }

    public static class VariantOptionResponse {
        private String name;
        private String value;
    }
}
