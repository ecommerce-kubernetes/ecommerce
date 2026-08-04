package com.example.order_service.order.application.port;

import com.example.order_service.order.application.service.order.dto.command.OrderSearchCommand;
import com.example.order_service.order.domain.order.Order;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface OrderRepository {
    Optional<Order> findByOrderIdAndOrdererId(Long orderId, Long ordererId);
    Order save(Order order);
    Page<Order> searchOrders(Long ordererId, OrderSearchCommand command);
}
