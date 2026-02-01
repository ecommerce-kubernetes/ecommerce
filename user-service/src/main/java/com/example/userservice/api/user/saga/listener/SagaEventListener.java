package com.example.userservice.api.user.saga.listener;

import com.example.common.user.UserSagaCommand;
import com.example.userservice.api.user.saga.service.SagaProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SagaEventListener {
    private final SagaProcessor sagaProcessor;

    @KafkaListener(topics = "${user.topics.user-saga-command}")
    public void handleOrderEvent(@Payload UserSagaCommand command) {
        sagaProcessor.userSagaProcess(command);
    }
}
