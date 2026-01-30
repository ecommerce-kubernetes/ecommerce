package com.example.order_service.api.cart.facade.dto.command;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddCartItemCommand {
    private Long userId;
    private Long productVariantId;
    private int quantity;

    @Builder
    private AddCartItemCommand(Long userId, Long productVariantId, int quantity){
        this.userId = userId;
        this.productVariantId = productVariantId;
        this.quantity = quantity;
    }

    public static AddCartItemCommand of(Long userId, Long productVariantId, int quantity){
        return AddCartItemCommand.builder()
                .userId(userId)
                .productVariantId(productVariantId)
                .quantity(quantity)
                .build();
    }
}
