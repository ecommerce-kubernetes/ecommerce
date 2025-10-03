package com.example.order_service.service.kafka;

public interface SagaCompensator {
    String getStepName();
    void compensate(Object rollbackEvent);
}
