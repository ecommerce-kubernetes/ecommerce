package com.example.order_service.repository;

import com.example.order_service.entity.Orders;
import com.example.order_service.repository.query.OrdersQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrdersRepository extends JpaRepository<Orders, Long>, OrdersQueryRepository {

    @Query("select o from orders o left join fetch o.orderItems where o.id = :orderId")
    Optional<Orders> findWithOrderItemsById(@Param("orderId") Long orderId);
}
