package com.example.order_service.order.adapter.out.persistence;

import com.example.order_service.order.domain.order.Order;
import com.example.order_service.order.domain.order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByIdAndOrderer_UserId(Long orderId, Long ordererId);
    List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime threshold);
}
