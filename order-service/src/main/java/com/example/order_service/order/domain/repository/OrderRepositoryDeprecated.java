package com.example.order_service.order.domain.repository;

import com.example.order_service.order.domain.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;

@Deprecated
public interface OrderRepositoryDeprecated extends JpaRepository<Order, Long> {
}
