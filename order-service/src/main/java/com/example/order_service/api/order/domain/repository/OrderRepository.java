package com.example.order_service.api.order.domain.repository;

import com.example.order_service.api.order.domain.model.Order;
import com.example.order_service.api.order.domain.repository.query.OrderQueryDslRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, OrderQueryDslRepository {
    @Query("select o from Order o where o.orderNo = :orderNo")
    Optional<Order> findByOrderNo(@Param("orderNo") String orderNo);
}
