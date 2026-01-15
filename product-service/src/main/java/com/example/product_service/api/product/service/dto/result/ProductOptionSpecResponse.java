package com.example.product_service.api.product.service.dto.result;

import com.example.product_service.api.product.domain.model.ProductOptionSpec;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class ProductOptionSpecResponse {
    private Long productId;
    private List<OptionSpecDto> specs;

    @Getter
    public static class OptionSpecDto {
        private Long id;
        private Long optionTypeId;
        private String optionTypeName;
        private Integer priority;

        @Builder
        private OptionSpecDto(Long id, Long optionTypeId, String optionTypeName, Integer priority) {
            this.id = id;
            this.optionTypeId = optionTypeId;
            this.optionTypeName = optionTypeName;
            this.priority = priority;
        }
    }

    @Builder
    private ProductOptionSpecResponse(Long productId, List<OptionSpecDto> specs) {
        this.productId = productId;
        this.specs = specs;
    }

    public static ProductOptionSpecResponse of(Long productId, List<ProductOptionSpec> optionSpecs) {
        List<OptionSpecDto> specs = optionSpecs.stream().map(optionSpec -> OptionSpecDto.builder().id(optionSpec.getId())
                .optionTypeId(optionSpec.getOptionType().getId())
                .optionTypeName(optionSpec.getOptionType().getName())
                .priority(optionSpec.getPriority())
                .build()).toList();
        return ProductOptionSpecResponse.builder().productId(productId).specs(specs)
                .build();
    }
}
