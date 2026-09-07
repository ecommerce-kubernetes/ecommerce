package com.example.product_service.product.adapter.in.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

@Builder
public record AddProductDescriptionImageResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING) Long productId
) {
    public static AddProductDescriptionImageResponse of(Long productId) {
        return AddProductDescriptionImageResponse.builder()
                .productId(productId)
                .build();
    }
}
