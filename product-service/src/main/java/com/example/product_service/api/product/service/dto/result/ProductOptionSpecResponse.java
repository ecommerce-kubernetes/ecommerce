package com.example.product_service.api.product.service.dto.result;

import com.example.product_service.api.product.domain.model.Product;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class ProductOptionSpecResponse {
    private Long productId;
    private List<OptionSpec> options;

    @Getter
    public static class OptionSpec {
        private Long productOptionSpecId;
        private Long optionTypeId;
        private String name;
        private Integer priority;

        @Builder
        private OptionSpec(Long productOptionSpecId, Long optionTypeId, String name, Integer priority) {
            this.productOptionSpecId = productOptionSpecId;
            this.optionTypeId = optionTypeId;
            this.name = name;
            this.priority = priority;
        }
    }

    @Builder
    private ProductOptionSpecResponse(Long productId, List<OptionSpec> options) {
        this.productId = productId;
        this.options = options;
    }

    public static ProductOptionSpecResponse from(Product product) {
        List<OptionSpec> options = product.getOptionSpecs().stream().map(optionSpec -> OptionSpec.builder().productOptionSpecId(optionSpec.getId())
                .optionTypeId(optionSpec.getOptionType().getId())
                .name(optionSpec.getOptionType().getName())
                .priority(optionSpec.getPriority())
                .build()).toList();
        return ProductOptionSpecResponse.builder().productId(product.getId())
                .options(options)
                .build();
    }
}
