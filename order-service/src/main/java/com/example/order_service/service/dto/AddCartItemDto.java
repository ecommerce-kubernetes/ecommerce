package com.example.order_service.service.dto;

import com.example.order_service.controller.dto.CartItemRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddCartItemDto {
    private Long userId;
    private Long productVariantId;
    private int quantity;

    @Builder
    private AddCartItemDto(Long userId, Long productVariantId, int quantity){
        this.userId = userId;
        this.productVariantId = productVariantId;
        this.quantity = quantity;
    }

    public static AddCartItemDto of(Long userId, Long productVariantId, int quantity){
        return AddCartItemDto.builder()
                .userId(userId)
                .productVariantId(productVariantId)
                .quantity(quantity)
                .build();
    }

    public static AddCartItemDto of(Long userId, CartItemRequest request){
        return AddCartItemDto.builder()
                .userId(userId)
                .productVariantId(request.getProductVariantId())
                .quantity(request.getQuantity())
                .build();
    }
}
