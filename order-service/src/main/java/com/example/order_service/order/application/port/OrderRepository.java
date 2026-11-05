package com.example.order_service.order.application.port;

import com.example.order_service.order.application.service.order.dto.command.OrderSearchCommand;
import com.example.order_service.order.domain.order.Order;
import com.example.order_service.order.domain.order.OrderStatus;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Optional<Order> findById(Long orderId);
    Optional<Order> findByOrderIdAndOrdererId(Long orderId, Long ordererId);
    List<Order> findOrdersByStatusAndCreatedAtBefore(OrderStatus orderStatus, LocalDateTime threshold);
    Order save(Order order);
    Page<Order> searchOrders(Long ordererId, OrderSearchCommand command);
}
