package com.example.order_service.cart.adapter.in.web.dto.response;

import com.example.order_service.cart.application.service.dto.result.AddCartItemsResult;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.util.List;

@Builder
public record AddCartItemsResponse(
        List<Item> items
) {

    @Builder
    public record Item(
            @JsonFormat(shape = JsonFormat.Shape.STRING)
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
