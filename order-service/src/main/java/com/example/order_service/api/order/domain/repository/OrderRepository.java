package com.example.order_service.api.order.domain.repository;

import com.example.order_service.api.order.domain.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("select o from Orders o left join fetch o.orderItems where o.id = :orderId")
    Optional<Order> findWithOrderItemsById(@Param("orderId") Long orderId);
}
