package com.example.product_service.product.adapter.in.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

@Builder
public record AddProductVariantResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING) Long productId
) {
    public static AddProductVariantResponse of(Long productId) {
        return AddProductVariantResponse.builder()
                .productId(productId)
                .build();
    }
}
