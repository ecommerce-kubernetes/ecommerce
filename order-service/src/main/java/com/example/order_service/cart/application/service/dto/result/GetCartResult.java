package com.example.order_service.cart.application.service.dto.result;

import lombok.Builder;

import java.util.List;

@Builder
public record GetCartResult(
        List<CartItemResult> items
) {

}
