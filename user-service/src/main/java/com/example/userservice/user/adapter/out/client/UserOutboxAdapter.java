package com.example.userservice.user.adapter.out.client;

import com.example.userservice.common.properties.SagaTopicProperties;
import com.example.userservice.outbox.application.service.OutboxCommandService;
import com.example.userservice.outbox.domain.context.CreateOutboxMessageContext;
import com.example.userservice.user.adapter.out.client.dto.PointSagaReplyPayload;
import com.example.userservice.user.adapter.out.client.dto.PointSagaReplyType;
import com.example.userservice.user.application.port.UserOutboxPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserOutboxAdapter implements UserOutboxPort {

    private final SagaTopicProperties topicProperties;

    private final OutboxCommandService outboxCommandService;

    private final ObjectMapper objectMapper;

    @Override
    public void recordForwardSuccess(Long sagaId, Long executionId) {
        saveOutboxMessage(sagaId, PointSagaReplyType.FORWARD, PointSagaReplyPayload.success(executionId));
    }

    @Override
    public void recordCompensateSuccess(Long sagaId, Long executionId) {
        saveOutboxMessage(sagaId, PointSagaReplyType.COMPENSATE, PointSagaReplyPayload.success(executionId));
    }

    @Override
    public void recordForwardFail(Long sagaId, Long executionId, String reason) {
        saveOutboxMessage(sagaId, PointSagaReplyType.FORWARD, PointSagaReplyPayload.fail(executionId, reason));
    }

    @Override
    public void recordCompensateFail(Long sagaId, Long executionId, String reason) {
        saveOutboxMessage(sagaId, PointSagaReplyType.COMPENSATE, PointSagaReplyPayload.fail(executionId, reason));
    }

    private void saveOutboxMessage(Long sagaId, PointSagaReplyType replyType, PointSagaReplyPayload payload) {
        try {
            Map<String, PointSagaReplyType> headerMap = Map.of("X-Reply-Type", replyType);

            String headerJson = objectMapper.writeValueAsString(headerMap);
            String payloadJson = objectMapper.writeValueAsString(payload);

            CreateOutboxMessageContext context = CreateOutboxMessageContext.builder()
                    .topic(topicProperties.order().reply())
                    .routingKey(String.valueOf(sagaId))
                    .headers(headerJson)
                    .payload(payloadJson)
                    .build();

            outboxCommandService.createOutbox(context);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
