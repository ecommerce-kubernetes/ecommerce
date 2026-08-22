package com.example.order_service.order.application.service.fixture;

import com.example.order_service.order.application.port.dto.OrderCartItemsResult;

import java.util.List;

public class OrderCartResultFixture {

    public static OrderCartItemsResult.OrderCartItemsResultBuilder anOrderCartItems() {
        OrderCartItemsResult.Item item = anOrderCartItem().build();

        return OrderCartItemsResult.builder()
                .items(List.of(item));
    }

    public static OrderCartItemsResult.Item.ItemBuilder anOrderCartItem() {
        return OrderCartItemsResult.Item.builder()
                .cartItemId(1L)
                .productVariantId(1L)
                .quantity(1);
    }
}
