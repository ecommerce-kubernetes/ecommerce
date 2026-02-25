package com.example.userservice.api.user.saga.producer;

import com.example.common.result.SagaProcessResult;
import com.example.userservice.api.user.saga.properties.UserTopicProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SagaEventProducer {

    private final UserTopicProperties userTopicProperties;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendSagaSuccess(Long sagaId, String orderNo) {
        SagaProcessResult result = SagaProcessResult.success(sagaId, orderNo);
        kafkaTemplate.send(userTopicProperties.getUserSagaReply(), String.valueOf(sagaId), result);
    }

    public void sendSagaFailure(Long sagaId, String orderNo, String errorCode, String failureReason) {
        SagaProcessResult result = SagaProcessResult.fail(sagaId, orderNo, errorCode, failureReason);
        kafkaTemplate.send(userTopicProperties.getUserSagaReply(), String.valueOf(sagaId), result);
    }
}
