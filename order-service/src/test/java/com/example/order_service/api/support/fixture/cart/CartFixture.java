package com.example.order_service.api.support.fixture.cart;

import com.example.order_service.api.cart.domain.service.dto.result.CartItemDto;

public class CartFixture {

    public static CartItemDto.CartItemDtoBuilder anCartItemDto() {
        return CartItemDto.builder()
                .id(1L)
                .productVariantId(1L)
                .quantity(1);
    }
}
