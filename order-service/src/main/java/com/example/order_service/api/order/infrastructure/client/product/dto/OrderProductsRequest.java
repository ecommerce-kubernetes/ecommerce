package com.example.order_service.api.order.infrastructure.client.product.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OrderProductsRequest {
    private List<Long> variantIds;

    public static OrderProductsRequest of(List<Long> variantIds) {
        return OrderProductsRequest.builder()
                .variantIds(variantIds)
                .build();
    }
}
