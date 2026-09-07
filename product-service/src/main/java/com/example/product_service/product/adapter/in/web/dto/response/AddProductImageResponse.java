package com.example.product_service.product.adapter.in.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

@Builder
public record AddProductImageResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING) Long productId
) {
    public static AddProductImageResponse of(Long productId) {
        return AddProductImageResponse.builder()
                .productId(productId)
                .build();
    }
}
