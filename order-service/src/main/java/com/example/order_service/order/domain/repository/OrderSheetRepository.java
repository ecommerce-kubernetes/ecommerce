package com.example.order_service.order.domain.repository;

import com.example.order_service.order.domain.model.OrderSheet;

import java.time.Duration;
import java.util.Optional;

public interface OrderSheetRepository {
    OrderSheet save(OrderSheet orderSheet, Duration ttl);
    Optional<OrderSheet> findById(String sheetId);
}
