package com.example.product_service.api.product.service.dto.result;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class ProductOptionSpecResponse {
    private Long productId;
    private List<ProductOptionSpec> options;

    @Getter
    public static class ProductOptionSpec {
        private Long productOptionId;
        private Long optionTypeId;
        private String name;
        private Integer priority;

        @Builder
        private ProductOptionSpec(Long productOptionId, Long optionTypeId, String name, Integer priority) {
            this.productOptionId = productOptionId;
            this.optionTypeId = optionTypeId;
            this.name = name;
            this.priority = priority;
        }
    }

    @Builder
    private ProductOptionSpecResponse(Long productId, List<ProductOptionSpec> options) {
        this.productId = productId;
        this.options = options;
    }
}
