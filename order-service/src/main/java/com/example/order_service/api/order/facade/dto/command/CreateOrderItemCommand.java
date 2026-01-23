package com.example.order_service.api.order.facade.dto.command;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrderItemCommand {
    private Long productVariantId;
    private int quantity;

    @Builder
    private CreateOrderItemCommand(Long productVariantId, int quantity){
        this.productVariantId = productVariantId;
        this.quantity = quantity;
    }

    public static CreateOrderItemCommand of(Long productVariantId, int quantity){
        return CreateOrderItemCommand.builder()
                .productVariantId(productVariantId)
                .quantity(quantity)
                .build();
    }
}
