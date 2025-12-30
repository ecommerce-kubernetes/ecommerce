package com.example.order_service.api.order.domain.repository;

import com.example.order_service.api.order.domain.model.Order;
import com.example.order_service.api.order.domain.repository.query.OrderQueryDslRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long>, OrderQueryDslRepository {
}
