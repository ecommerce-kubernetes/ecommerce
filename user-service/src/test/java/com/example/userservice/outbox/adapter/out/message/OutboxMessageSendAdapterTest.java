package com.example.userservice.outbox.adapter.out.message;

import com.example.userservice.common.exception.PortException;
import com.example.userservice.outbox.application.service.dto.OutboxMessageResult;
import com.example.userservice.outbox.domain.OutboxFixtureBuilder;
import com.example.userservice.outbox.domain.OutboxMessage;
import com.example.userservice.outbox.exception.OutboxPortErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class OutboxMessageSendAdapterTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private OutboxMessageSendAdapter outboxMessageSendAdapter;

    @Captor
    private ArgumentCaptor<Message<String>> messageCaptor;

    @BeforeEach
    void setUp() {
        outboxMessageSendAdapter = new OutboxMessageSendAdapter(kafkaTemplate, objectMapper);
    }

    @Test
    @DisplayName("아웃박스 메시지를 카프카 메시지로 변환해 발행한다.")
    void send() {
        //given
        OutboxMessage outboxMessage = OutboxFixtureBuilder.given().build();
        OutboxMessageResult result = OutboxMessageResult.from(outboxMessage);
        //when
        outboxMessageSendAdapter.send(result);
        //then
        then(kafkaTemplate).should(times(1)).send(messageCaptor.capture());

        Message<String> sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getPayload()).isEqualTo(result.payload());
        assertThat(sentMessage.getHeaders().get(KafkaHeaders.TOPIC)).isEqualTo(result.topic());
        assertThat(sentMessage.getHeaders().get(KafkaHeaders.KEY)).isEqualTo(result.routingKey());
        assertThat(sentMessage.getHeaders().get("X-Reply-Type")).isEqualTo("FORWARD");
    }

    @Test
    @DisplayName("헤더 역직렬화에 실패하면 예외가 발생한다.")
    void send_whenHeaderDeserializationFails_thenThrownException() {
        //given
        OutboxMessage outboxMessage = OutboxFixtureBuilder.given().build();
        OutboxMessageResult result = OutboxMessageResult.builder()
                .id(outboxMessage.getId())
                .topic(outboxMessage.getTopic())
                .routingKey(outboxMessage.getRoutingKey())
                .headers("invalid-json")
                .payload(outboxMessage.getPayload())
                .build();
        //when
        //then
        assertThatThrownBy(() -> outboxMessageSendAdapter.send(result))
                .isInstanceOf(PortException.class)
                .extracting("errorCode")
                .isEqualTo(OutboxPortErrorCode.MESSAGE_DESERIALIZATION_ERROR);
    }
}
