package com.example.order_service.cart.api.dto.response;

import com.example.order_service.cart.application.dto.result.AddCartItemsResult;
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
        public static Item from(AddCartItemsResult.CartItemResult item) {
            return Item.builder()
                    .cartItemId(item.cartItemId())
                    .productVariantId(item.productVariantId())
                    .quantity(item.quantity())
                    .build();
        }

        public static List<Item> from(List<AddCartItemsResult.CartItemResult> items) {
            return items.stream().map(Item::from).toList();
        }
    }

    public static AddCartItemsResponse from(AddCartItemsResult result) {
        return AddCartItemsResponse.builder()
                .items(Item.from(result.items()))
                .build();
    }
}
