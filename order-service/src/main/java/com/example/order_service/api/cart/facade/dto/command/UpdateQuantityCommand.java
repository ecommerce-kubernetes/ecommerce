package com.example.order_service.api.cart.facade.dto.command;

import com.example.order_service.api.cart.controller.dto.request.UpdateQuantityRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateQuantityCommand {
    private Long userId;
    private Long cartItemId;
    private int quantity;

    @Builder
    private UpdateQuantityCommand(Long userId, Long cartItemId, int quantity){
        this.userId = userId;
        this.cartItemId = cartItemId;
        this.quantity = quantity;
    }

    public static UpdateQuantityCommand of(Long userId, Long cartItemId, UpdateQuantityRequest request){
        return UpdateQuantityCommand.builder()
                .userId(userId)
                .cartItemId(cartItemId)
                .quantity(request.getQuantity())
                .build();
    }
}
