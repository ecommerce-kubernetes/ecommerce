package com.example.order_service.cart.application.dto.result;

import com.example.order_service.cart.application.dto.data.CartItemData;
import lombok.Builder;

import java.util.List;

@Builder
public record AddCartItemsResult(
        List<AddedItemResult> items
) {

    @Builder
    public record AddedItemResult(
            Long cartItemId,
            Long productVariantId,
            int quantity
    ) {

        public static AddedItemResult from(CartItemData data) {
            return AddedItemResult.builder()
                    .cartItemId(data.cartItemId())
                    .productVariantId(data.productVariantId())
                    .quantity(data.quantity())
                    .build();
        }

        public static List<AddedItemResult> from(List<CartItemData> data) {
            return data.stream().map(AddedItemResult::from).toList();
        }
    }

    public static AddCartItemsResult from(List<CartItemData> data) {
        return AddCartItemsResult.builder()
                .items(AddedItemResult.from(data))
                .build();
    }
}
