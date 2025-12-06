package com.example.order_service.api.order.domain.repository.query;

import com.example.order_service.api.order.domain.model.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrdersQueryRepository {
    Page<Orders> findAllByParameter(Pageable pageable, Long userId, Integer year, String keyword);
}
