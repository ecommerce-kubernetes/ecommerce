package com.example.order_service.api.support.fixture.cart;

import com.example.order_service.api.cart.facade.dto.command.AddCartItemCommand;
import com.example.order_service.api.cart.facade.dto.command.UpdateQuantityCommand;

public class CartCommandFixture {

    public static AddCartItemCommand.AddCartItemCommandBuilder anAddCartItemCommand() {
        return AddCartItemCommand.builder()
                .userId(1L)
                .productVariantId(1L)
                .quantity(1);
    }

    public static UpdateQuantityCommand.UpdateQuantityCommandBuilder anUpdateQuantityCommand() {
        return UpdateQuantityCommand.builder()
                .userId(1L)
                .cartItemId(1L)
                .quantity(1);
    }
}
