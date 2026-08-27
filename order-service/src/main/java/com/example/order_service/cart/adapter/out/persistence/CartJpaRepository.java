package com.example.order_service.cart.adapter.out.persistence;

import com.example.order_service.cart.domain.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartJpaRepository extends JpaRepository<Cart, Long> {
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems WHERE c.userId = :userId")
    Optional<Cart> findWithItemsByUserId(@Param("userId") Long userId);
    Optional<Cart> findByUserId(Long userId);
}
