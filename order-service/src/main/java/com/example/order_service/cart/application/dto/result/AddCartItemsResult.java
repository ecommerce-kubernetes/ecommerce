package com.example.order_service.cart.application.dto.result;

import com.example.order_service.cart.application.dto.data.CartItemData;
import lombok.Builder;

import java.util.List;

@Builder
public record AddCartItemsResult(
        List<CartItemResult> items
) {

    @Builder
    public record CartItemResult(
            Long cartItemId,
            Long productVariantId,
            int quantity
    ) {

        public static CartItemResult from(CartItemData data) {
            return CartItemResult.builder()
                    .cartItemId(data.cartItemId())
                    .productVariantId(data.productVariantId())
                    .quantity(data.quantity())
                    .build();
        }

        public static List<CartItemResult> from(List<CartItemData> data) {
            return data.stream().map(CartItemResult::from).toList();
        }
    }

    public static AddCartItemsResult from(List<CartItemData> data) {
        return AddCartItemsResult.builder()
                .items(CartItemResult.from(data))
                .build();
    }
}
