package com.example.order_service.cart.application.port;

import com.example.order_service.cart.application.port.dto.CartProductResult;

import java.util.List;

public interface CartProductPort {
    CartProductResult getProducts(List<Long> productVariantIds);
}
