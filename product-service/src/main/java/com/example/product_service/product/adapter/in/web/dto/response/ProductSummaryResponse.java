package com.example.product_service.product.adapter.in.web.dto.response;

import com.example.product_service.product.domain.model.ProductStatus;
import com.example.product_service.product.application.service.dto.result.ProductResult;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ProductSummaryResponse(
        Long productId,
        String name,
        String thumbnail,
        Long displayPrice,
        Long originalPrice,
        Integer maxDiscountRate,
        Long categoryId,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
        LocalDateTime publishedAt,
        Double rating,
        Long reviewCount,
        ProductStatus status
) {
    public static ProductSummaryResponse from(ProductResult.Summary result) {
        return ProductSummaryResponse.builder()
                .productId(result.productId())
                .name(result.name())
                .thumbnail(result.thumbnail())
                .displayPrice(result.displayPrice())
                .originalPrice(result.originalPrice())
                .maxDiscountRate(result.maxDiscountRate())
                .categoryId(result.categoryId())
                .publishedAt(result.publishedAt())
                .rating(result.rating())
                .reviewCount(result.reviewCount())
                .status(result.status())
                .build();
    }
}
