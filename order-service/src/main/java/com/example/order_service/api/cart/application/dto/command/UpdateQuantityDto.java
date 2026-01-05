package com.example.order_service.api.cart.application.dto.command;

import com.example.order_service.api.cart.controller.dto.request.UpdateQuantityRequest;
import com.example.order_service.api.common.security.principal.UserPrincipal;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateQuantityDto {
    private Long userId;
    private Long cartItemId;
    private int quantity;

    @Builder
    private UpdateQuantityDto(Long userId, Long cartItemId, int quantity){
        this.userId = userId;
        this.cartItemId = cartItemId;
        this.quantity = quantity;
    }

    public static UpdateQuantityDto of(Long userId, Long cartItemId, UpdateQuantityRequest request){
        return UpdateQuantityDto.builder()
                .userId(userId)
                .cartItemId(cartItemId)
                .quantity(request.getQuantity())
                .build();
    }
}
