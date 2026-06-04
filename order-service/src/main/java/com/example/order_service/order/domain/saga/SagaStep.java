package com.example.order_service.order.domain.saga;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SagaStep {
    INVENTORY_DEDUCT_PENDING("재고 감소"),
    INVENTORY_RESTORE_PENDING("재고 복구");

    private final String description;
}
