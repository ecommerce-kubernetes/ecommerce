package com.example.userservice.user.adapter.out.client;

import com.example.userservice.common.properties.SagaTopicProperties;
import com.example.userservice.outbox.application.service.OutboxCommandService;
import com.example.userservice.outbox.domain.context.CreateOutboxMessageContext;
import com.example.userservice.user.adapter.out.client.dto.PointSagaReplyPayload;
import com.example.userservice.user.adapter.out.client.dto.PointSagaReplyType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class UserOutboxAdapterTest {

    private static final String REPLY_TOPIC = "user.point.saga.reply";

    private UserOutboxAdapter userOutboxAdapter;

    @Mock
    private OutboxCommandService outboxCommandService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        SagaTopicProperties topicProperties = new SagaTopicProperties(
                new SagaTopicProperties.TopicConfig("user.point.saga.command", REPLY_TOPIC)
        );
        userOutboxAdapter = new UserOutboxAdapter(topicProperties, outboxCommandService, objectMapper);
    }

    @Test
    @DisplayName("정방향 성공을 아웃박스 메시지로 기록한다.")
    void recordForwardSuccess() throws Exception {
        //given
        Long sagaId = 1L;
        Long executionId = 2L;
        //when
        userOutboxAdapter.recordForwardSuccess(sagaId, executionId);
        //then
        CreateOutboxMessageContext context = captureOutboxContext();
        assertThat(context.topic()).isEqualTo(REPLY_TOPIC);
        assertThat(context.routingKey()).isEqualTo(String.valueOf(sagaId));
        assertThat(context.headers()).isEqualTo(objectMapper.writeValueAsString(Map.of("X-Reply-Type", PointSagaReplyType.FORWARD)));
        assertThat(context.payload()).isEqualTo(objectMapper.writeValueAsString(PointSagaReplyPayload.success(executionId)));
    }

    @Test
    @DisplayName("보상 성공을 아웃박스 메시지로 기록한다.")
    void recordCompensateSuccess() throws Exception {
        //given
        Long sagaId = 1L;
        Long executionId = 2L;
        //when
        userOutboxAdapter.recordCompensateSuccess(sagaId, executionId);
        //then
        CreateOutboxMessageContext context = captureOutboxContext();
        assertThat(context.topic()).isEqualTo(REPLY_TOPIC);
        assertThat(context.routingKey()).isEqualTo(String.valueOf(sagaId));
        assertThat(context.headers()).isEqualTo(objectMapper.writeValueAsString(Map.of("X-Reply-Type", PointSagaReplyType.COMPENSATE)));
        assertThat(context.payload()).isEqualTo(objectMapper.writeValueAsString(PointSagaReplyPayload.success(executionId)));
    }

    @Test
    @DisplayName("정방향 실패를 아웃박스 메시지로 기록한다.")
    void recordForwardFail() throws Exception {
        //given
        Long sagaId = 1L;
        Long executionId = 2L;
        String reason = "포인트 부족";
        //when
        userOutboxAdapter.recordForwardFail(sagaId, executionId, reason);
        //then
        CreateOutboxMessageContext context = captureOutboxContext();
        assertThat(context.headers()).isEqualTo(objectMapper.writeValueAsString(Map.of("X-Reply-Type", PointSagaReplyType.FORWARD)));
        assertThat(context.payload()).isEqualTo(objectMapper.writeValueAsString(PointSagaReplyPayload.fail(executionId, reason)));
    }

    @Test
    @DisplayName("보상 실패를 아웃박스 메시지로 기록한다.")
    void recordCompensateFail() throws Exception {
        //given
        Long sagaId = 1L;
        Long executionId = 2L;
        String reason = "환불 실패";
        //when
        userOutboxAdapter.recordCompensateFail(sagaId, executionId, reason);
        //then
        CreateOutboxMessageContext context = captureOutboxContext();
        assertThat(context.headers()).isEqualTo(objectMapper.writeValueAsString(Map.of("X-Reply-Type", PointSagaReplyType.COMPENSATE)));
        assertThat(context.payload()).isEqualTo(objectMapper.writeValueAsString(PointSagaReplyPayload.fail(executionId, reason)));
    }

    private CreateOutboxMessageContext captureOutboxContext() {
        ArgumentCaptor<CreateOutboxMessageContext> captor = ArgumentCaptor.forClass(CreateOutboxMessageContext.class);
        then(outboxCommandService).should().createOutbox(captor.capture());
        return captor.getValue();
    }
}
