package com.example.order_service.order.application.port;

import com.example.order_service.order.domain.ordersheet.OrderSheet;

import java.time.Duration;
import java.util.Optional;

public interface OrderSheetRepository {
    OrderSheet save(OrderSheet orderSheet, Duration ttl);
    Optional<OrderSheet> findById(Long orderSheetId);
    Optional<OrderSheet> findByIdAndOrdererId(Long orderSheetId, Long ordererId);
}
