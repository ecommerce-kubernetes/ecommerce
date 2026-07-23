package com.example.order_service.cart.api.dto.response;

import com.example.order_service.cart.application.dto.result.UpdateCartItemQuantityResult;
import lombok.Builder;

@Builder
public record UpdateCartItemQuantityResponse(
        Long cartItemId,
        Long productVariantId,
        Integer quantity
) {

    public static UpdateCartItemQuantityResponse from(UpdateCartItemQuantityResult result) {
        return UpdateCartItemQuantityResponse.builder()
                .cartItemId(result.cartItemId())
                .productVariantId(result.productVariantId())
                .quantity(result.quantity())
                .build();
    }
}
