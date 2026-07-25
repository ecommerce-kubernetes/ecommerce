package com.example.order_service.cart.api.dto.response;

import com.example.order_service.cart.application.dto.result.UpdateCartItemQuantityResult;
import lombok.Builder;

@Builder
public record UpdateCartItemQuantityResponse(
        Long cartItemId
) {

    public static UpdateCartItemQuantityResponse from(UpdateCartItemQuantityResult result) {
        return UpdateCartItemQuantityResponse.builder()
                .cartItemId(result.cartItemId())
                .build();
    }
}
