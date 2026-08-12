package com.example.order_service.cart.adaptor.in.web.dto.response;

import com.example.order_service.cart.application.service.dto.result.UpdateCartItemQuantityResult;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

@Builder
public record UpdateCartItemQuantityResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long cartItemId
) {

    public static UpdateCartItemQuantityResponse from(UpdateCartItemQuantityResult result) {
        return UpdateCartItemQuantityResponse.builder()
                .cartItemId(result.cartItemId())
                .build();
    }
}
