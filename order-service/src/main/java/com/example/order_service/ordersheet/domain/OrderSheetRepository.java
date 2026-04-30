package com.example.order_service.ordersheet.domain;

import java.time.Duration;

public interface OrderSheetRepository {
    OrderSheet save(OrderSheet orderSheet, Duration ttl);
}
