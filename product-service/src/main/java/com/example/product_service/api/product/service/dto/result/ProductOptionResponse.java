package com.example.product_service.api.product.service.dto.result;

import com.example.product_service.api.product.domain.model.ProductOption;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class ProductOptionResponse {
    private Long productId;
    private List<OptionDto> options;

    @Getter
    public static class OptionDto {
        private Long optionTypeId;
        private String optionTypeName;
        private Integer priority;

        @Builder
        private OptionDto(Long optionTypeId, String optionTypeName, Integer priority) {
            this.optionTypeId = optionTypeId;
            this.optionTypeName = optionTypeName;
            this.priority = priority;
        }
    }

    @Builder
    private ProductOptionResponse(Long productId, List<OptionDto> options) {
        this.productId = productId;
        this.options = options;
    }

    public static ProductOptionResponse of(Long productId, List<ProductOption> optionSpecs) {
        List<OptionDto> specs = optionSpecs.stream().map(optionSpec -> OptionDto.builder()
                .optionTypeId(optionSpec.getOptionType().getId())
                .optionTypeName(optionSpec.getOptionType().getName())
                .priority(optionSpec.getPriority())
                .build()).toList();
        return ProductOptionResponse.builder().productId(productId).options(specs)
                .build();
    }
}
