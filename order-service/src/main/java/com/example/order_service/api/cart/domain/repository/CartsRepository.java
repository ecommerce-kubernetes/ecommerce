package com.example.order_service.api.cart.domain.repository;

import com.example.order_service.api.cart.domain.model.Carts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartsRepository extends JpaRepository<Carts, Long> {
    @Query("SELECT c FROM Carts c LEFT JOIN FETCH c.cartItems WHERE c.userId = :userId")
    Optional<Carts> findWithItemsByUserId(@Param("userId") Long userId);
    Optional<Carts> findByUserId(Long userId);
}
