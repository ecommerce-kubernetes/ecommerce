package com.example.product_service.product.adapter.in.web.dto.response;

import com.example.product_service.product.application.service.dto.result.ProductSummaryResult;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

@Builder
public record ProductSummaryResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING) Long productId,
        String name,
        String thumbnail,
        Long originalPrice,
        Integer discountRate,
        Long price,
        Long reviewCount
) {
    public static ProductSummaryResponse from(ProductSummaryResult result) {
        return ProductSummaryResponse.builder()
                .productId(result.productId())
                .name(result.name())
                .thumbnail(result.thumbnail())
                .originalPrice(result.originalPrice())
                .discountRate(result.discountRate())
                .price(result.price())
                .reviewCount(result.reviewCount())
                .build();
    }
}
