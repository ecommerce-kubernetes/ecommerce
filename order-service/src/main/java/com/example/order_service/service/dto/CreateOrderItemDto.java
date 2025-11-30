package com.example.order_service.service.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrderItemDto {
    private Long productVariantId;
    private int quantity;

    @Builder
    private CreateOrderItemDto(Long productVariantId, int quantity){
        this.productVariantId = productVariantId;
        this.quantity = quantity;
    }

    public static CreateOrderItemDto of(Long productVariantId, int quantity){
        return CreateOrderItemDto.builder()
                .productVariantId(productVariantId)
                .quantity(quantity)
                .build();
    }
}
