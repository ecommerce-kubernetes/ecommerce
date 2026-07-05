package com.example.order_service.cart.application.external.dto;

import java.util.List;

public record CartProductListResult(
        List<CartProductResult> products
) {
}
