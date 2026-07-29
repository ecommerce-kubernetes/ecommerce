package com.example.order_service.order.domain.repository;

import com.example.order_service.order.application.service.order.dto.command.OrderSearchCommand;
import com.example.order_service.order.domain.order.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderSearchRepository {
    Page<Order> searchOrders(Long userId, OrderSearchCommand command, Pageable pageable);
    List<Order> findOrdersBefore(LocalDateTime threshold, int size);
}
