package com.example.order_service.cart.application.dto.result;

import lombok.Builder;

import java.util.List;

@Builder
public record CartResult(
        List<CartItemResult> items
) {

}
