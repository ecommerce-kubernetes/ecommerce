package com.example.order_service.cart.application.service.dto.result;

import lombok.Builder;

import java.util.List;

@Builder
public record AddCartItemsResult(
        List<AddedItemResult> items
) {

    @Builder
    public record AddedItemResult(
            Long cartItemId
    ) {

        public static AddedItemResult from(Long cartItemId) {
            return AddedItemResult.builder()
                    .cartItemId(cartItemId)
                    .build();
        }

        public static List<AddedItemResult> from(List<Long> cartItemIds) {
            return cartItemIds.stream().map(AddedItemResult::from).toList();
        }
    }

    public static AddCartItemsResult from(List<Long> cartItemIds) {
        return AddCartItemsResult.builder()
                .items(AddedItemResult.from(cartItemIds))
                .build();
    }
}
