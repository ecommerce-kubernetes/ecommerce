package com.example.order_service.order.application.port;

import com.example.order_service.order.domain.order.Order;

import java.util.Optional;

public interface OrderRepository {
    Optional<Order> findByOrderIdAndOrdererId(Long orderId, Long ordererId);
    Order save(Order order);
}
