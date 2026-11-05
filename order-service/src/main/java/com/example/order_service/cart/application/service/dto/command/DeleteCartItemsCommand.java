package com.example.order_service.cart.application.service.dto.command;

import lombok.Builder;

import java.util.List;

@Builder
public record DeleteCartItemsCommand(
        Long userId,
        List<Long> cartItemIds
) {

    public static DeleteCartItemsCommand of(Long userId, List<Long> cartItemIds) {
        return DeleteCartItemsCommand.builder()
                .userId(userId)
                .cartItemIds(cartItemIds)
                .build();
    }
}
