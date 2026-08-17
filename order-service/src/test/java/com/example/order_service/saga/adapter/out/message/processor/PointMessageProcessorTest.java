package com.example.order_service.saga.adapter.out.message.processor;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.saga.adapter.out.message.processor.dto.SagaCommandType;
import com.example.order_service.saga.domain.event.UsedPointEvent;
import com.example.order_service.support.annotation.MockRedis;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.Message;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = {
        "spring.cloud.bus.enabled=false",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
@EmbeddedKafka(partitions = 1, topics = { "user-saga-command" })
@MockRedis
class PointMessageProcessorTest {

    @Autowired
    private PointMessageProcessor processor;

    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Captor
    private ArgumentCaptor<Message<String>> messageCaptor;

    @Test
    @DisplayName("포인트 차감 이벤트 수신시 포인트 차감 메시지를 발행한다.")
    void process_whenReceiveUsedPointEvent_thenPublishUsedPointCommand() {
        //given
        UsedPointEvent usedEvent = createUsedEvent();
        //when
        processor.process(usedEvent);
        //then
        verify(kafkaTemplate, times(1)).send(messageCaptor.capture());
        Message<String> capturedMessage = messageCaptor.getValue();

        assertThat(capturedMessage.getHeaders().get(KafkaHeaders.KEY)).isEqualTo("1");
        assertThat(capturedMessage.getHeaders().get("X-Saga-Id")).isEqualTo(1L);
        assertThat(capturedMessage.getHeaders().get("X-Command-Type")).isEqualTo(SagaCommandType.USE_POINT.name());
        assertThat(capturedMessage.getHeaders().containsKey(KafkaHeaders.TOPIC)).isTrue();

        String payload = capturedMessage.getPayload();
        assertThat(payload).contains("\"executionId\":1");
        assertThat(payload).contains("\"userId\":1");
        assertThat(payload).contains("\"usedPoints\":1000");
    }

    private UsedPointEvent createUsedEvent() {
        return UsedPointEvent.builder()
                .sagaId(1L)
                .orderId(1L)
                .executionId(1L)
                .userId(1L)
                .usedPoints(Money.wons(1000L))
                .build();
    }

}