package com.example.order_service.cart.api.dto.response;

import com.example.order_service.cart.application.dto.result.CartItemResult;
import com.example.order_service.cart.application.dto.result.CartResult;
import lombok.Builder;

import java.util.List;

@Builder
public record AddCartItemsResponse(
        List<Item> items
) {

    @Builder
    public record Item(
            Long cartItemId,
            Long productVariantId,
            int quantity
    ) {
        public static Item from(CartItemResult item) {
            return Item.builder()
                    .cartItemId(item.cartItemId())
                    .productVariantId(item.productVariantId())
                    .quantity(item.quantity())
                    .build();
        }

        public static List<Item> from(List<CartItemResult> items) {
            return items.stream().map(Item::from).toList();
        }
    }

    public static AddCartItemsResponse from(CartResult result) {
        return AddCartItemsResponse.builder()
                .items(Item.from(result.items()))
                .build();
    }
}
