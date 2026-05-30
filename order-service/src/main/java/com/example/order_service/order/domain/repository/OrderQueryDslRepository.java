package com.example.order_service.order.domain.repository;

import com.example.order_service.order.application.service.order.dto.command.OrderSearchCommand;
import com.example.order_service.order.domain.model.Order;
import org.springframework.data.domain.Page;

public interface OrderQueryDslRepository {

    Page<Order> findByUserId(Long userId, OrderSearchCommand command);
}
