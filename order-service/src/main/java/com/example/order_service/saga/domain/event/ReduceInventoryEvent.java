package com.example.order_service.saga.domain.event;

import com.example.order_service.saga.domain.OrderSagaPayload;
import lombok.Builder;

import java.util.List;

@Builder
public record ReduceInventoryEvent(
        Long orderId,
        Long executionId,
        List<OrderSagaPayload.OrderLine> orderLines
) {
}
