package com.example.order_service.order.domain.repository.query;

import com.example.order_service.order.api.dto.request.OrderSearchCondition;
import com.example.order_service.order.domain.model.Order;
import org.springframework.data.domain.Page;

public interface OrderQueryDslRepository {
    Page<Order> findByUserIdAndCondition(Long userId, OrderSearchCondition condition);
}
