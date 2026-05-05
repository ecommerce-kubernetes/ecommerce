package com.example.order_service.cart.domain.repository;

import com.example.order_service.cart.domain.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Modifying
    @Query("delete from CartItem ci where ci.id in :cartItemIds and ci.cart.userId = :userId")
    int deleteAllByIdsAndUserId(@Param("userId") Long userId, @Param("cartItemIds") List<Long> cartItemIds);
}
