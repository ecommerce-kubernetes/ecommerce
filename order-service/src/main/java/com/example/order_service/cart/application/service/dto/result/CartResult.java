package com.example.order_service.cart.application.service.dto.result;

import lombok.Builder;

import java.util.Collections;
import java.util.List;

@Builder
public record CartResult(
        List<CartItemResult> items
) {

    public static CartResult empty() {
        return CartResult.builder()
                .items(Collections.emptyList())
                .build();
    }
}
