package com.example.order_service.api.order.domain.repository;

import com.example.order_service.api.order.domain.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
