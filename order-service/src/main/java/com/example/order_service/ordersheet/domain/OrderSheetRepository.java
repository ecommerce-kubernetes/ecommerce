package com.example.order_service.ordersheet.domain;

import java.util.Optional;

public interface OrderSheetRepository {
    Optional<OrderSheet> save(OrderSheet orderSheet);
}
