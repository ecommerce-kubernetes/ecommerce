package com.example.order_service.api.cart.domain.repository;

import com.example.order_service.api.cart.domain.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
