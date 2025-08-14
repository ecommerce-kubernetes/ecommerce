package com.example.order_service.repository.query;

import com.example.order_service.entity.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrdersQueryRepository {
    Page<Orders> findAllByParameter(Pageable pageable, Long userId, Integer year, String keyword);
}
