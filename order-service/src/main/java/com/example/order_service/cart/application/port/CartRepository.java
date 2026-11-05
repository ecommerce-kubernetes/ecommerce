package com.example.order_service.cart.application.port;

import com.example.order_service.cart.domain.Cart;

import java.util.Optional;

public interface CartRepository {
    Optional<Cart> findWithItemsByUserId(Long userId);
    Optional<Cart> findByUserId(Long userId);
    Cart save(Cart cart);
}
