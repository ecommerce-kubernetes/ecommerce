package com.example.order_service.saga.adapter.out.message.processor;

import com.example.order_service.saga.domain.event.SagaEvent;

public interface SagaMessageProcessor {

    boolean supports(SagaEvent event);
    void process(SagaEvent event);
}
