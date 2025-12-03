package com.example.order_service.repository;

import com.example.order_service.entity.OrderItems;
import com.example.order_service.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemsRepository extends JpaRepository<OrderItems, Long> {
    @Query("SELECT oi FROM OrderItems oi WHERE oi.order.id = :orderId")
    List<OrderItems> findByOrderId(@Param("orderId") Long orderId);

    @Modifying
    @Query("delete from CartItems ci where ci.cart.id = :cartId")
    void deleteByCartId(@Param("cartId") Long cartId);
}
