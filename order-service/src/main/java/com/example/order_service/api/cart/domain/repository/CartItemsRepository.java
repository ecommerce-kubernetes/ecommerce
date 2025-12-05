package com.example.order_service.api.cart.domain.repository;

import com.example.order_service.api.cart.domain.model.CartItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartItemsRepository extends JpaRepository<CartItems, Long> {
    @Query("SELECT ci FROM CartItems ci JOIN FETCH ci.cart WHERE ci.id = :cartItemId")
    Optional<CartItems> findWithCartById(@Param("cartItemId") Long cartItemId);
}
