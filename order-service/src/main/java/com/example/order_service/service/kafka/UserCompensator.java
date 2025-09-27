package com.example.order_service.service.kafka;

import com.example.common.UserCashDeductedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserCompensator implements SagaCompensator{
    private final ObjectMapper mapper;
    private final KafkaProducer kafkaProducer;
    private static final String USER_ROLLBACK_TOPIC = "user.cash.restore";
    @Override
    public String getStepName() {
        return "user";
    }

    @Override
    public void compensate(Object rollbackEvent) {
        UserCashDeductedEvent event = mapper.convertValue(rollbackEvent, UserCashDeductedEvent.class);
        kafkaProducer.sendMessage(USER_ROLLBACK_TOPIC, event);
    }
}
