package com.example.product_service.product.adapter.in.web.dto.response;

import com.example.product_service.product.application.service.dto.result.ProductResult;
import com.example.product_service.product.domain.model.ProductStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PublishProductResponse(
        Long productId,
        ProductStatus status,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
        LocalDateTime publishedAt
) {
    public static PublishProductResponse from(ProductResult.Publish result) {
        return PublishProductResponse.builder()
                .productId(result.productId())
                .status(result.status())
                .publishedAt(result.publishedAt())
                .build();
    }
}
