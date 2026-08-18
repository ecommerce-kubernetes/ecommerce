package com.example.order_service.cart.domain.fixture;

import com.example.order_service.cart.domain.Cart;
import com.example.order_service.common.util.TsidGenerator;

public class CartFixture {
    public static Cart createEmptyCart() {
        return Cart.create(1L, new TsidGenerator());
    }
}
