package com.example.order_service.order.infrastructure.messaging;

import com.example.order_service.order.application.orchestrator.OrderSagaManager;
import com.example.order_service.saga.domain.tmp.SagaStep;
import com.example.order_service.order.infrastructure.messaging.dto.SagaReplyMessage;
import com.example.order_service.order.infrastructure.messaging.dto.SagaResult;
import com.example.order_service.support.annotation.MockRedis;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.Message;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
@MockRedis
@EmbeddedKafka(topics = {"${order.topics.order-saga-reply}"}, partitions = 1)
class SagaReplyListenerTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private OrderSagaManager orderSagaManager;
    @Value("${order.topics.order-saga-reply}")
    private String REPLY_TOPIC;

    @Test
    @DisplayName("SAGA 처리 메시지를 수신하면 SagaManager를 호출한다")
    void handleSagaReply() throws JsonProcessingException {
        //given
        Long sagaId = 1L;
        String orderNo = "orderNo";
        SagaReplyMessage replyPayload = SagaReplyMessage.builder()
                .result(SagaResult.SUCCESS)
                .orderNo(orderNo)
                .step(SagaStep.INVENTORY_DEDUCT_PENDING)
                .code("SUCCESS")
                .build();

        String jsonPayload = objectMapper.writeValueAsString(replyPayload);
        Message<String> kafkaMessage = MessageBuilder
                .withPayload(jsonPayload)
                .setHeader(KafkaHeaders.TOPIC, REPLY_TOPIC)
                .setHeader(KafkaHeaders.KEY, orderNo)
                .setHeader("X-SAGA-ID", String.valueOf(sagaId))
                .build();
        //when
        kafkaTemplate.send(kafkaMessage);
        //then
        ArgumentCaptor<SagaReplyMessage> messageCaptor = ArgumentCaptor.forClass(SagaReplyMessage.class);
        verify(orderSagaManager, timeout(5000)).handleReply(messageCaptor.capture());
        SagaReplyMessage capturedMessage = messageCaptor.getValue();

        assertThat(capturedMessage.getSagaId()).isEqualTo(sagaId);
        assertThat(capturedMessage.getOrderNo()).isEqualTo(orderNo);
        assertThat(capturedMessage.getResult()).isEqualTo(SagaResult.SUCCESS);
    }
}