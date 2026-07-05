package com.example.order_service.cart.api.dto.request;

import com.example.order_service.cart.application.dto.command.UpdateCartItemQuantityCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record UpdateCartItemQuantityRequest(
        @NotNull(message = "수량은 필수값 입니다")
        @Min(value = 1, message = "수량은 1이상이여야 합니다")
        Integer quantity
) {

    public UpdateCartItemQuantityCommand toCommand(Long userId, Long cartItemId) {
        return UpdateCartItemQuantityCommand.builder()
                .userId(userId)
                .cartItemId(cartItemId)
                .quantity(quantity)
                .build();
    }
}
