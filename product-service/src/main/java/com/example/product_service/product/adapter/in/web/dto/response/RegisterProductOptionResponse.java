package com.example.product_service.product.adapter.in.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

@Builder
public record RegisterProductOptionResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING) Long productId
) {
    public static RegisterProductOptionResponse of(Long productId) {
        return RegisterProductOptionResponse.builder()
                .productId(productId)
                .build();
    }
}
