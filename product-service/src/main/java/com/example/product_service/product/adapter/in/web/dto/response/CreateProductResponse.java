package com.example.product_service.product.adapter.in.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

@Builder
public record CreateProductResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING) Long productId
) {
    public static CreateProductResponse of(Long productId) {
        return CreateProductResponse.builder()
                .productId(productId)
                .build();
    }
}
