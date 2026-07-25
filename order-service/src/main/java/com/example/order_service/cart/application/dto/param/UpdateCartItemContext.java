package com.example.order_service.cart.application.dto.param;

import com.example.order_service.cart.application.dto.command.UpdateCartItemQuantityCommand;
import com.example.order_service.cart.application.dto.data.CartItemData;
import com.example.order_service.cart.application.port.dto.CartProductResult;
import lombok.Builder;

import java.util.Map;

@Builder
public record UpdateCartItemContext(
        Long userId,
        Long cartItemId,
        Integer quantity,
        Integer maxLimit
) {

    public static UpdateCartItemContext of(CartItemData cartItem, UpdateCartItemQuantityCommand command, CartProductResult products) {
        Map<Long, CartProductResult.CartProductDetail> map = products.toMap();
        CartProductResult.CartProductDetail product = map.get(cartItem.productVariantId());
        return UpdateCartItemContext.builder()
                .userId(command.userId())
                .cartItemId(cartItem.cartItemId())
                .quantity(command.quantity())
                .maxLimit(product.stock())
                .build();
    }
}
