package com.example.order_service.cart.domain.service.dto.result;

import com.example.order_service.cart.domain.model.CartItem;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

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

    public static CartItemDto from(CartItem cartItem){
        return CartItemDto.builder()
                .id(cartItem.getId())
                .productVariantId(cartItem.getProductVariantId())
                .quantity(cartItem.getQuantity())
                .build();
    }

    public static List<CartItemDto> from(List<CartItem> cartItems) {
        return cartItems.stream().map(CartItemDto::from).toList();
    }
}
