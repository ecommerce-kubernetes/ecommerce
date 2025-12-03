package com.example.order_service.service.dto;

import com.example.order_service.common.security.UserPrincipal;
import com.example.order_service.controller.dto.UpdateQuantityRequest;
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
