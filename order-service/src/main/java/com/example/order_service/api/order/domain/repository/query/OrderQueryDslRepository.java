package com.example.order_service.api.order.domain.repository.query;

import com.example.order_service.api.order.controller.dto.request.OrderSearchCondition;
import com.example.order_service.api.order.domain.model.Order;
import org.springframework.data.domain.Page;

public interface OrderQueryDslRepository {
    Page<Order> findByUserIdAndCondition(Long userId, OrderSearchCondition condition);
}
