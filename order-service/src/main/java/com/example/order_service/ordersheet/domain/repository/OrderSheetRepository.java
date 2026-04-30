package com.example.order_service.ordersheet.domain.repository;

import com.example.order_service.ordersheet.domain.model.OrderSheet;

import java.time.Duration;

public interface OrderSheetRepository {
    OrderSheet save(OrderSheet orderSheet, Duration ttl);
}
