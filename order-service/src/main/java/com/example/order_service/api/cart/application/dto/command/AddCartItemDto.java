package com.example.order_service.api.cart.application.dto.command;

import com.example.order_service.api.common.security.principal.UserPrincipal;
import com.example.order_service.api.cart.controller.dto.request.CartItemRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddCartItemDto {
    private UserPrincipal userPrincipal;
    private Long productVariantId;
    private int quantity;

    @Builder
    private AddCartItemDto(UserPrincipal userPrincipal, Long productVariantId, int quantity){
        this.userPrincipal = userPrincipal;
        this.productVariantId = productVariantId;
        this.quantity = quantity;
    }

    public static AddCartItemDto of(UserPrincipal userPrincipal, Long productVariantId, int quantity){
        return AddCartItemDto.builder()
                .userPrincipal(userPrincipal)
                .productVariantId(productVariantId)
                .quantity(quantity)
                .build();
    }

    public static AddCartItemDto of(UserPrincipal userPrincipal, CartItemRequest request){
        return AddCartItemDto.builder()
                .userPrincipal(userPrincipal)
                .productVariantId(request.getProductVariantId())
                .quantity(request.getQuantity())
                .build();
    }
}
