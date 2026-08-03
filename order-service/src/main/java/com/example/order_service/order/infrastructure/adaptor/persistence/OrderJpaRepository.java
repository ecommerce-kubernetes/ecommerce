package com.example.order_service.order.infrastructure.adaptor.persistence;

import com.example.order_service.order.domain.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByIdAndOrderer_UserId(Long orderId, Long ordererId);
}
