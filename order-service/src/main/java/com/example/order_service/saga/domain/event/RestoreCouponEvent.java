package com.example.order_service.saga.domain.event;

import com.example.order_service.saga.domain.OrderSagaPayload;
import lombok.Builder;

@Builder
public record RestoreCouponEvent(
        Long sagaId,
        Long orderId,
        Long executionId,
        Long userId,
        OrderSagaPayload.UsedCoupons coupons
) implements SagaEvent {
}
