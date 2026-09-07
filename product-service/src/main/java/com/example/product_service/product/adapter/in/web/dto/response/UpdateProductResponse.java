package com.example.product_service.product.adapter.in.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

@Builder
public record UpdateProductResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING) Long productId
) {
    public static UpdateProductResponse of(Long productId) {
        return UpdateProductResponse.builder()
                .productId(productId)
                .build();
    }
}
