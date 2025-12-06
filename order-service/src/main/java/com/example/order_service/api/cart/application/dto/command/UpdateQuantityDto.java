package com.example.order_service.api.cart.application.dto.command;

import com.example.order_service.api.common.security.principal.UserPrincipal;
import com.example.order_service.api.cart.controller.dto.request.UpdateQuantityRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateQuantityDto {
    private UserPrincipal userPrincipal;
    private Long cartItemId;
    private int quantity;

    @Builder
    private UpdateQuantityDto(UserPrincipal userPrincipal, Long cartItemId, int quantity){
        this.userPrincipal = userPrincipal;
        this.cartItemId = cartItemId;
        this.quantity = quantity;
    }

    public static UpdateQuantityDto of(UserPrincipal userPrincipal, Long cartItemId, UpdateQuantityRequest request){
        return UpdateQuantityDto.builder()
                .userPrincipal(userPrincipal)
                .cartItemId(cartItemId)
                .quantity(request.getQuantity())
                .build();
    }
}
