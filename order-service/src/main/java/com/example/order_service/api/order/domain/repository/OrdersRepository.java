package com.example.order_service.api.order.domain.repository;

import com.example.order_service.api.order.domain.model.Orders;
import com.example.order_service.api.order.domain.repository.query.OrdersQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrdersRepository extends JpaRepository<Orders, Long>, OrdersQueryRepository {

    @Query("select o from Orders o left join fetch o.orderItems where o.id = :orderId")
    Optional<Orders> findWithOrderItemsById(@Param("orderId") Long orderId);
}
