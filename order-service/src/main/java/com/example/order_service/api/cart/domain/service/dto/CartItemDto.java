package com.example.order_service.api.cart.domain.service.dto;

import com.example.order_service.api.cart.domain.model.CartItems;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CartItemDto {
    private Long id;
    private Long productVariantId;
    private int quantity;

    @Builder
    private CartItemDto(Long id, Long productVariantId, int quantity){
        this.id = id;
        this.productVariantId = productVariantId;
        this.quantity = quantity;
    }

    public static CartItemDto of(CartItems cartItem){
        return of(cartItem.getId(), cartItem.getProductVariantId(), cartItem.getQuantity());
    }

    public static CartItemDto of(Long id, Long productVariantId, int quantity){
        return CartItemDto.builder()
                .id(id)
                .productVariantId(productVariantId)
                .quantity(quantity)
                .build();
    }
}
