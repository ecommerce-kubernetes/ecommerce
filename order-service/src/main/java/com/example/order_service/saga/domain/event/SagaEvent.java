package com.example.order_service.saga.domain.event;

public interface SagaEvent {
    Long sagaId();
    Long orderId();
    Long executionId();
}
