package com.example.order_service.service.kafka;

import com.example.common.CouponUsedSuccessEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouponCompensator implements SagaCompensator{
    private final ObjectMapper mapper;
    private final KafkaProducer kafkaProducer;
    private static final String COUPON_ROLLBACK_TOPIC = "coupon.used.cancel";
    @Override
    public String getStepName() {
        return "coupon";
    }

    @Override
    public void compensate(Object rollbackEvent) {
        CouponUsedSuccessEvent event = mapper.convertValue(rollbackEvent, CouponUsedSuccessEvent.class);
        kafkaProducer.sendMessage(COUPON_ROLLBACK_TOPIC, event.getOrderId().toString(), event);
    }
}
