package com.example.product_service.product.adapter.in.web.dto.response;

import com.example.product_service.product.domain.model.ProductStatus;
import com.example.product_service.product.application.service.dto.result.ProductResult;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CloseProductResponse(
        Long productId,
        ProductStatus status,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
        LocalDateTime saleStoppedAt
) {
    public static CloseProductResponse from(ProductResult.Close result) {
        return CloseProductResponse.builder()
                .productId(result.productId())
                .status(result.status())
                .saleStoppedAt(result.saleStoppedAt())
                .build();
    }
}
