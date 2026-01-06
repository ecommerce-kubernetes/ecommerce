package com.example.order_service.api.cart.domain.repository;

import com.example.order_service.api.cart.domain.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.cart WHERE ci.id = :cartItemId")
    Optional<CartItem> findWithCartById(@Param("cartItemId") Long cartItemId);
}
