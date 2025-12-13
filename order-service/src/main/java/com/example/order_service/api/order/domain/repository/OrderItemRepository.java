package com.example.order_service.api.order.domain.repository;

import com.example.order_service.api.order.domain.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @Query("SELECT oi FROM OrderItems oi WHERE oi.order.id = :orderId")
    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);

    @Modifying
    @Query("delete from CartItems ci where ci.cart.id = :cartId")
    void deleteByCartId(@Param("cartId") Long cartId);
}
