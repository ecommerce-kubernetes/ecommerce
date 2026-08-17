package com.example.order_service.saga.adapter.out.message.processor;

import com.example.order_service.saga.domain.OrderSagaPayload;
import com.example.order_service.saga.domain.event.ReduceInventoryEvent;
import com.example.order_service.saga.domain.event.RestoreInventoryEvent;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = {
        "spring.cloud.bus.enabled=false",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
@EmbeddedKafka(partitions = 1, topics = { "product-saga-command" })
@MockRedis
class InventoryMessageProcessorTest {

    @Autowired
    private InventoryMessageProcessor processor;

    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Captor
    private ArgumentCaptor<Message<String>> messageCaptor;

    @Test
    @DisplayName("재고 차감 이벤트 수신시 재고 차감 메시지를 발행한다.")
    void process_whenReceiveReduceInventoryEvent_thenPublishReduceInventoryCommand() {
        //given
        ReduceInventoryEvent reduceEvent = createReduceEvent();
        //when
        processor.process(reduceEvent);
        //then
        verify(kafkaTemplate, times(1)).send(messageCaptor.capture());
        Message<String> capturedMessage = messageCaptor.getValue();

        assertThat(capturedMessage.getHeaders().get(KafkaHeaders.KEY)).isEqualTo("1");
        assertThat(capturedMessage.getHeaders().get("X-Saga-Id")).isEqualTo(1L);
        assertThat(capturedMessage.getHeaders().get("X-Event-Type")).isEqualTo("ReduceInventoryCommand");
        assertThat(capturedMessage.getHeaders().containsKey(KafkaHeaders.TOPIC)).isTrue();

        String payload = capturedMessage.getPayload();
        assertThat(payload).contains("\"executionId\":1");
        assertThat(payload).contains("\"productVariantId\":1");
        assertThat(payload).contains("\"quantity\":1");
    }

    @Test
    @DisplayName("재고 복구 이벤트 수신시 재고 복구 메시지를 발행한다.")
    void process_whenRestoreInventoryEvent_thenPublishRestoreInventoryCommand() {
        //given
        RestoreInventoryEvent restoreEvent = createRestoreEvent();
        //when
        processor.process(restoreEvent);
        //then
        verify(kafkaTemplate, times(1)).send(messageCaptor.capture());
        Message<String> capturedMessage = messageCaptor.getValue();

        assertThat(capturedMessage.getHeaders().get(KafkaHeaders.KEY)).isEqualTo("1");
        assertThat(capturedMessage.getHeaders().get("X-Saga-Id")).isEqualTo(1L);
        assertThat(capturedMessage.getHeaders().get("X-Event-Type")).isEqualTo("RestoreInventoryCommand");
        assertThat(capturedMessage.getHeaders().containsKey(KafkaHeaders.TOPIC)).isTrue();

        String payload = capturedMessage.getPayload();
        assertThat(payload).contains("\"executionId\":1");
        assertThat(payload).contains("\"productVariantId\":1");
        assertThat(payload).contains("\"quantity\":1");
    }

    private ReduceInventoryEvent createReduceEvent() {
        OrderSagaPayload.OrderLine orderLine = OrderSagaPayload.OrderLine.builder()
                .productVariantId(1L)
                .quantity(1)
                .build();

        return ReduceInventoryEvent.builder()
                .sagaId(1L)
                .orderId(1L)
                .executionId(1L)
                .orderLines(List.of(orderLine))
                .build();
    }

    private RestoreInventoryEvent createRestoreEvent() {
        OrderSagaPayload.OrderLine orderLine = OrderSagaPayload.OrderLine.builder()
                .productVariantId(1L)
                .quantity(1)
                .build();

        return RestoreInventoryEvent.builder()
                .sagaId(1L)
                .orderId(1L)
                .executionId(1L)
                .orderLines(List.of(orderLine))
                .build();
    }

}