package com.example.order_service.cart.adaptor.in.web.dto.request;

import com.example.order_service.cart.application.service.dto.command.UpdateCartItemQuantityCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record UpdateCartItemQuantityRequest(
        @NotNull(message = "{cart.item.quantity.notNull}")
        @Min(value = 1, message = "{cart.item.quantity.min}")
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
