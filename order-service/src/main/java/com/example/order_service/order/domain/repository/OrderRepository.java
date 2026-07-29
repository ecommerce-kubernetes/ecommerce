package com.example.order_service.order.domain.repository;

import com.example.order_service.order.domain.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNoAndOrderer_UserId(String orderNo, Long userId);
    Optional<Order> findByOrderNo(String orderNo);
}
