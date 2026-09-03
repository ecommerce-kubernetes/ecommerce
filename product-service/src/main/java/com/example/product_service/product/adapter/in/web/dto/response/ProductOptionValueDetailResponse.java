package com.example.product_service.product.adapter.in.web.dto.response;

import com.example.product_service.product.application.service.dto.result.ProductResult;
import lombok.Builder;

@Builder
public record ProductOptionValueDetailResponse(
        Long optionValueId,
        String name
) {
    public static ProductOptionValueDetailResponse from(ProductResult.OptionValueDetail result) {
        return ProductOptionValueDetailResponse.builder()
                .optionValueId(result.optionValueId())
                .name(result.name())
                .build();
    }
}
