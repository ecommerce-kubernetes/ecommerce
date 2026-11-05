package com.example.order_service.saga.domain.event;

import com.example.order_service.common.domain.vo.Money;
import lombok.Builder;

@Builder
public record UsedPointEvent(
        Long sagaId,
        Long orderId,
        Long executionId,
        Long userId,
        Money usedPoints
) implements SagaEvent {
}
