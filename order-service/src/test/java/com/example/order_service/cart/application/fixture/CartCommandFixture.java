package com.example.order_service.cart.application.fixture;

import com.example.order_service.cart.application.service.dto.command.AddCartItemsCommand;
import com.example.order_service.cart.application.service.dto.command.DeleteCartItemsCommand;
import com.example.order_service.cart.application.service.dto.command.UpdateCartItemQuantityCommand;

import java.util.List;

public class CartCommandFixture {

    public static AddCartItemsCommand.AddCartItemsCommandBuilder anAddCommand() {
        return AddCartItemsCommand.builder()
                .userId(1L)
                .items(List.of(anItem().build()));
    }

    public static AddCartItemsCommand.Item.ItemBuilder anItem() {
        return AddCartItemsCommand.Item.builder()
                .productVariantId(1L)
                .quantity(3);
    }

    public static UpdateCartItemQuantityCommand.UpdateCartItemQuantityCommandBuilder anUpdateQuantityCommand() {
        return UpdateCartItemQuantityCommand.builder()
                .userId(1L)
                .cartItemId(1L)
                .quantity(5);
    }

    public static DeleteCartItemsCommand.DeleteCartItemsCommandBuilder anDeleteCommand() {
        return DeleteCartItemsCommand.builder()
                .userId(1L)
                .cartItemIds(List.of(1L));
    }
}
