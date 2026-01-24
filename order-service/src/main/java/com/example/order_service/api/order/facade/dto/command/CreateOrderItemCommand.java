package com.example.order_service.api.order.facade.dto.command;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
public class CreateOrderItemCommand {
    private Long productVariantId;
    private int quantity;

    public static CreateOrderItemCommand of(Long productVariantId, int quantity){
        return CreateOrderItemCommand.builder()
                .productVariantId(productVariantId)
                .quantity(quantity)
                .build();
    }
}
