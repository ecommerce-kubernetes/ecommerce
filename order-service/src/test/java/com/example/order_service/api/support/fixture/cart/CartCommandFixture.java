package com.example.order_service.api.support.fixture.cart;

import com.example.order_service.api.cart.facade.dto.command.AddCartItemCommand;

public class CartCommandFixture {

    public static AddCartItemCommand.AddCartItemCommandBuilder anAddCartItemCommand() {
        return AddCartItemCommand.builder()
                .userId(1L)
                .productVariantId(1L)
                .quantity(1);
    }
}
