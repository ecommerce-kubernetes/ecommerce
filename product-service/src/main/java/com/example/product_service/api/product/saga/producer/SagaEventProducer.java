package com.example.product_service.api.product.saga.producer;

import com.example.common.result.SagaProcessResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SagaEventProducer {

    @Value("${product.topics.saga-reply}")
    private String productResultTopic;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendSagaSuccess(Long sagaId, String orderNo) {
        SagaProcessResult result = SagaProcessResult.success(sagaId, orderNo);
        kafkaTemplate.send(productResultTopic, String.valueOf(sagaId), result);
    }

    public void sendSagaFailure(Long sagaId, String orderNo, String errorCode, String failureReason) {
        SagaProcessResult result = SagaProcessResult.fail(sagaId, orderNo, errorCode, failureReason);
        kafkaTemplate.send(productResultTopic, String.valueOf(sagaId), result);
    }

}
