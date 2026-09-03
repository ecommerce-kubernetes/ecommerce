package com.example.userservice.outbox.adapter.out.message;

import com.example.userservice.outbox.application.service.dto.OutboxMessageResult;
import com.example.userservice.outbox.domain.OutboxFixtureBuilder;
import com.example.userservice.outbox.domain.OutboxMessage;
import com.example.userservice.support.annotation.MockRedis;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.cloud.bus.enabled=false",
        "spring.cloud.stream.enabled=false"
})
@EmbeddedKafka(partitions = 1, topics = {"order.saga.reply", "user.saga.command"})
@MockRedis
class OutboxMessageSendAdapterIntegrationTest {

    private static final String TOPIC = "order.saga.reply";

    @Autowired
    private OutboxMessageSendAdapter outboxMessageSendAdapter;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private Consumer<String, String> consumer;

    @BeforeEach
    void setUp() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("outbox-send-test-group", "true", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumer = new KafkaConsumer<>(consumerProps, new StringDeserializer(), new StringDeserializer());
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, TOPIC);
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    @Test
    @DisplayName("아웃박스 메시지를 실제 카프카에 발행하면 토픽/키/헤더/페이로드가 그대로 전달된다.")
    void send() {
        //given
        OutboxMessage outboxMessage = OutboxFixtureBuilder.given().build();
        OutboxMessageResult result = OutboxMessageResult.from(outboxMessage);
        //when
        outboxMessageSendAdapter.send(result);
        //then
        ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumer, TOPIC, Duration.ofSeconds(10));

        assertThat(record.topic()).isEqualTo(result.topic());
        assertThat(record.key()).isEqualTo(result.routingKey());
        assertThat(record.value()).isEqualTo(result.payload());

        Header replyTypeHeader = record.headers().lastHeader("X-Reply-Type");
        assertThat(replyTypeHeader).isNotNull();
        assertThat(new String(replyTypeHeader.value(), StandardCharsets.UTF_8)).isEqualTo("FORWARD");
    }
}
