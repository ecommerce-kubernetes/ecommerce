package com.example.product_service.product.adapter.in.web.dto.response;

import com.example.product_service.product.application.service.dto.result.ProductResult;
import lombok.Builder;

import java.util.List;

@Builder
public record ProductOptionGroupResponse(
        Long optionTypeId,
        String name,
        Integer priority,
        List<ProductOptionValueDetailResponse> values
) {
    public static ProductOptionGroupResponse from(ProductResult.OptionGroup optionGroup) {
        return ProductOptionGroupResponse.builder()
                .optionTypeId(optionGroup.optionTypeId())
                .name(optionGroup.name())
                .priority(optionGroup.priority())
                .values(optionGroup.values().stream().map(ProductOptionValueDetailResponse::from).toList())
                .build();
    }
}
