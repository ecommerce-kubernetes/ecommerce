package com.example.order_service.saga.adapter.out.message.processor;

import com.example.order_service.saga.domain.event.SagaEvent;
import com.example.order_service.saga.domain.event.UsedPointEvent;
import org.springframework.stereotype.Component;

@Component
public class PointMessageProcessor implements SagaMessageProcessor{
    @Override
    public boolean supports(SagaEvent event) {
        return event instanceof UsedPointEvent;
    }

    @Override
    public void process(SagaEvent event) {

    }
}
