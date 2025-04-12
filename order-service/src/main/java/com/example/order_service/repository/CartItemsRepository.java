package com.example.order_service.repository;

import com.example.order_service.entity.CartItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CartItemsRepository extends JpaRepository<CartItems, Long> {
    @Query("SELECT ci FROM CartItems ci WHERE ci.productId = :productId")
    List<CartItems> findByProductId(@Param("productId") Long productId);
}
