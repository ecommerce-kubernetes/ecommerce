package com.example.order_service.cart.application.fixture;

import com.example.order_service.cart.application.service.dto.data.CartItemData;

public class CartDataFixture {

    public static CartItemData.CartItemDataBuilder anCartItemData() {
        return CartItemData.builder()
                .cartItemId(1L)
                .productVariantId(1L)
                .quantity(3);
    }
}
