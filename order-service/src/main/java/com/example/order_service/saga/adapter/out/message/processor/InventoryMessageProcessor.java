package com.example.order_service.saga.adapter.out.message.processor;

import com.example.order_service.saga.domain.event.SagaEvent;

public class InventoryMessageProcessor implements SagaMessageProcessor{
    @Override
    public void process(SagaEvent event) {

    }

    @Override
    public boolean supports(SagaEvent event) {
        return false;
    }
}
