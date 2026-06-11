package com.example.order_service.order.application.messaging;

import com.example.order_service.order.application.messaging.dto.SagaMessage;
import com.example.order_service.order.domain.saga.SagaStatus;
import com.example.order_service.order.domain.saga.SagaStep;
import com.example.order_service.support.annotation.MockRedis;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.time.Duration;
import java.util.Map;

import static com.example.order_service.support.TestFixtureUtil.fixtureMonkey;
import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@MockRedis
@EmbeddedKafka(topics = {"${order.topics.product-saga-command}"}, partitions = 1)
class SagaMessageDispatcherTest {

    @Autowired
    private SagaMessageDispatcher dispatcher;
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    @Autowired
    private ObjectMapper objectMapper;
    private Consumer<String, String> consumer;

    @Value("${order.topics.product-saga-command}")
    private String INVENTORY_TOPIC;

    @BeforeEach
    void setUp() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-group", "true", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        DefaultKafkaConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(
                consumerProps, new StringDeserializer(), new StringDeserializer()
        );

        consumer = cf.createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, INVENTORY_TOPIC);
    }

    @AfterEach
    void tearDown() {
        if (consumer != null) {
            consumer.close();
        }
    }

    @Nested
    @DisplayName("재고 메시지 발행")
    class InventoryCommand {

        @Test
        @DisplayName("재고 차감 메시지를 발행한다")
        void dispatch_inventory_deduct() throws JsonProcessingException {
            //given
            SagaMessage message = fixtureMonkey.giveMeBuilder(SagaMessage.class)
                    .set("status", SagaStatus.STARTED)
                    .set("step", SagaStep.INVENTORY_DEDUCT_PENDING)
                    .sample();
            //when
            dispatcher.dispatch(message);
            //then
            ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(
                    consumer,
                    INVENTORY_TOPIC,
                    Duration.ofSeconds(5)
            );
            assertThat(record).isNotNull();
            String payload = record.value();
            assertThat(payload).contains("INVENTORY_DEDUCT_PENDING");
            SagaMessage publishedMessage = objectMapper.readValue(payload, SagaMessage.class);
            assertThat(publishedMessage.getStep()).isEqualTo(SagaStep.INVENTORY_DEDUCT_PENDING);
        }
    }
}