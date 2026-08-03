package com.example.order_service.order.domain.repository;

import com.example.order_service.order.domain.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
