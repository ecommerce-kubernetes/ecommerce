package com.example.order_service.repository;

import com.example.order_service.entity.Carts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartsRepository extends JpaRepository<Carts, Long> {
    @Query("SELECT c FROM Carts c LEFT JOIN FETCH c.cartItems WHERE c.userId = :userId")
    Optional<Carts> findByUserId(@Param("userId") Long userId);
}
