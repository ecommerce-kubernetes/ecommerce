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
            Long cartItemId
    ) {
        public static Item from(AddCartItemsResult.AddedItemResult item) {
            return Item.builder()
                    .cartItemId(item.cartItemId())
                    .build();
        }

        public static List<Item> from(List<AddCartItemsResult.AddedItemResult> items) {
            return items.stream().map(Item::from).toList();
        }
    }

    public static AddCartItemsResponse from(AddCartItemsResult result) {
        return AddCartItemsResponse.builder()
                .items(Item.from(result.items()))
                .build();
    }
}
