package com.example.order_service.order.domain.repository;

import com.example.order_service.order.application.service.order.dto.command.OrderSearchCommand;
import com.example.order_service.order.domain.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderSearchRepository {
    Page<Order> searchOrders(Long userId, OrderSearchCommand command, Pageable pageable);
}
