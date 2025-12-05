package com.example.order_service.api.cart.domain.service.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
public class CartItemDto {
    private Long id;
    private Long productVariantId;
    private int quantity;

    @Builder
    private CartItemDto(Long id, Long productVariantId, int quantity){
        this.id = id;
        this.productVariantId = productVariantId;
        this.quantity = quantity;
    }
}
