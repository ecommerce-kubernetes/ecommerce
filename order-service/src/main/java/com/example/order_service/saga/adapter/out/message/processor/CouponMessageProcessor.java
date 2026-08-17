package com.example.order_service.saga.adapter.out.message.processor;

import com.example.order_service.saga.domain.event.RestoreCouponEvent;
import com.example.order_service.saga.domain.event.SagaEvent;
import com.example.order_service.saga.domain.event.UsedCouponEvent;
import org.springframework.stereotype.Component;

@Component
public class CouponMessageProcessor implements SagaMessageProcessor {
    @Override
    public boolean supports(SagaEvent event) {
        return event instanceof UsedCouponEvent ||
                event instanceof RestoreCouponEvent;
    }

    @Override
    public void process(SagaEvent event) {

    }
}
