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
    private int quantity;

    @Builder
    private UpdateQuantityDto(UserPrincipal userPrincipal, int quantity){
        this.userPrincipal = userPrincipal;
        this.quantity = quantity;
    }

    public static UpdateQuantityDto of(UserPrincipal userPrincipal, UpdateQuantityRequest request){
        return UpdateQuantityDto.builder()
                .userPrincipal(userPrincipal)
                .quantity(request.getQuantity())
                .build();
    }
}
