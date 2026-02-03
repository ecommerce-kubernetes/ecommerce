package com.example.order_service.api.cart.infrastructure.client.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CartProductsRequest {
    private List<Long> variantIds;

    public static CartProductsRequest of(List<Long> variantIds) {
        return CartProductsRequest.builder()
                .variantIds(variantIds)
                .build();
    }

}
