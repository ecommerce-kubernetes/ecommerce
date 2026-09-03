package com.example.product_service.product.adapter.in.web.dto.response;

import com.example.product_service.product.application.service.dto.result.ProductResult;
import lombok.Builder;

@Builder
public record ProductOptionInfoResponse(
        Long optionTypeId,
        String optionTypeName,
        Integer priority
) {
    public static ProductOptionInfoResponse from(ProductResult.Option option) {
        return ProductOptionInfoResponse.builder()
                .optionTypeId(option.optionTypeId())
                .optionTypeName(option.optionTypeName())
                .priority(option.priority())
                .build();
    }
}
