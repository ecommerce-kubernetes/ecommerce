package com.example.order_service.order.application.messaging;

import com.example.order_service.order.application.messaging.dto.SagaMessage;
import com.example.order_service.saga.domain.SagaStatus;
import com.example.order_service.saga.domain.SagaStep;
import com.example.order_service.support.annotation.MockRedis;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.instancio.Instancio;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;


@Slf4j
@SpringBootTest
@MockRedis
@EmbeddedKafka(topics = {"${order.topics.product-saga-command}", "${order.topics.coupon-saga-command}", "${order.topics.user-saga-command}"}, partitions = 1)
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
    @Value("${order.topics.coupon-saga-command}")
    private String COUPON_TOPIC;
    @Value("${order.topics.user-saga-command}")
    private String USER_TOPIC;

    @BeforeEach
    void setUp() {
        String randomGroupId = "test-group-" + java.util.UUID.randomUUID();
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(randomGroupId, "true", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        DefaultKafkaConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(
                consumerProps, new StringDeserializer(), new StringDeserializer()
        );

        consumer = cf.createConsumer();
        embeddedKafkaBroker.consumeFromEmbeddedTopics(
                consumer,
                INVENTORY_TOPIC,
                COUPON_TOPIC,
                USER_TOPIC
        );
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
            Long sagaId = 1L;
            String orderNo = UUID.randomUUID().toString();
            SagaMessage message = Instancio.of(SagaMessage.class)
                    .set(field("sagaId"), sagaId)
                    .set(field("orderNo"), orderNo)
                    .set(field("status"), SagaStatus.STARTED)
                    .set(field("step"), SagaStep.INVENTORY_DEDUCT_PENDING)
                    .create();
            //when
            dispatcher.dispatch(message);
            //then
            ConsumerRecord<String, String> record = getRecord(INVENTORY_TOPIC, orderNo);
            Header sagaIdHeader = record.headers().lastHeader("X-SAGA-ID");
            assertThat(record).isNotNull();
            assertThat(record.key()).isEqualTo(orderNo);
            assertThat(sagaIdHeader).isNotNull();
            String payload = record.value();
            assertThat(payload).contains("INVENTORY_DEDUCT_PENDING");
            SagaMessage publishedMessage = objectMapper.readValue(payload, SagaMessage.class);
            assertThat(publishedMessage.getStep()).isEqualTo(SagaStep.INVENTORY_DEDUCT_PENDING);
        }

        @Test
        @DisplayName("재고 복구 메시지를 발행한다")
        void dispatch_inventory_restore() throws JsonProcessingException {
            //given
            Long sagaId = 1L;
            String orderNo = UUID.randomUUID().toString();
            SagaMessage message = Instancio.of(SagaMessage.class)
                    .set(field("sagaId"), sagaId)
                    .set(field("orderNo"), orderNo)
                    .set(field("status"), SagaStatus.COMPENSATING)
                    .set(field("step"), SagaStep.INVENTORY_RESTORE_PENDING)
                    .create();
            //when
            dispatcher.dispatch(message);
            //then
            ConsumerRecord<String, String> record = getRecord(INVENTORY_TOPIC, orderNo);
            Header sagaIdHeader = record.headers().lastHeader("X-SAGA-ID");
            assertThat(record).isNotNull();
            assertThat(record.key()).isEqualTo(orderNo);
            assertThat(sagaIdHeader).isNotNull();
            String payload = record.value();
            assertThat(payload).contains("INVENTORY_RESTORE_PENDING");
            SagaMessage publishedMessage = objectMapper.readValue(payload, SagaMessage.class);
            assertThat(publishedMessage.getStep()).isEqualTo(SagaStep.INVENTORY_RESTORE_PENDING);
        }
    }

    @Nested
    @DisplayName("쿠폰 메시지 발행")
    class CouponCommand {

        @Test
        @DisplayName("쿠폰 무효화 메시지를 발행한다")
        void dispatch_coupon_used() throws JsonProcessingException {
            //given
            Long sagaId = 1L;
            String orderNo = UUID.randomUUID().toString();
            SagaMessage message = Instancio.of(SagaMessage.class)
                    .set(field("sagaId"), sagaId)
                    .set(field("orderNo"), orderNo)
                    .set(field("status"), SagaStatus.STARTED)
                    .set(field("step"), SagaStep.COUPON_USE_PENDING)
                    .create();
            //when
            dispatcher.dispatch(message);
            //then
            ConsumerRecord<String, String> record = getRecord(COUPON_TOPIC, orderNo);
            Header sagaIdHeader = record.headers().lastHeader("X-SAGA-ID");
            assertThat(record).isNotNull();
            assertThat(record.key()).isEqualTo(orderNo);
            assertThat(sagaIdHeader).isNotNull();
            String payload = record.value();
            assertThat(payload).contains("COUPON_USE_PENDING");
            SagaMessage publishedMessage = objectMapper.readValue(payload, SagaMessage.class);
            assertThat(publishedMessage.getStep()).isEqualTo(SagaStep.COUPON_USE_PENDING);
        }

        @Test
        @DisplayName("쿠폰 복구 메시지를 발행한다")
        void dispatch_coupon_restore() throws JsonProcessingException {
            //given
            Long sagaId = 1L;
            String orderNo = UUID.randomUUID().toString();
            SagaMessage message = Instancio.of(SagaMessage.class)
                    .set(field("sagaId"), sagaId)
                    .set(field("orderNo"), orderNo)
                    .set(field("status"), SagaStatus.COMPENSATING)
                    .set(field("step"), SagaStep.COUPON_RESTORE_PENDING)
                    .create();
            //when
            dispatcher.dispatch(message);
            //then
            ConsumerRecord<String, String> record = getRecord(COUPON_TOPIC, orderNo);
            Header sagaIdHeader = record.headers().lastHeader("X-SAGA-ID");
            assertThat(record).isNotNull();
            assertThat(record.key()).isEqualTo(orderNo);
            assertThat(sagaIdHeader).isNotNull();
            String payload = record.value();
            assertThat(payload).contains("COUPON_RESTORE_PENDING");
            SagaMessage publishedMessage = objectMapper.readValue(payload, SagaMessage.class);
            assertThat(publishedMessage.getStep()).isEqualTo(SagaStep.COUPON_RESTORE_PENDING);
        }
    }

    @Nested
    @DisplayName("포인트 메시지 발행")
    class UserCommand {

        @Test
        @DisplayName("포인트 차감 메시지를 발행한다")
        void dispatch_point_deduct() throws JsonProcessingException {
            //given
            Long sagaId = 1L;
            String orderNo = UUID.randomUUID().toString();
            SagaMessage message = Instancio.of(SagaMessage.class)
                    .set(field("sagaId"), sagaId)
                    .set(field("orderNo"), orderNo)
                    .set(field("status"), SagaStatus.STARTED)
                    .set(field("step"), SagaStep.POINTS_DEDUCT_PENDING)
                    .create();
            //when
            dispatcher.dispatch(message);
            //then
            ConsumerRecord<String, String> record = getRecord(USER_TOPIC, orderNo);
            Header sagaIdHeader = record.headers().lastHeader("X-SAGA-ID");
            assertThat(record).isNotNull();
            assertThat(record.key()).isEqualTo(orderNo);
            assertThat(sagaIdHeader).isNotNull();
            String payload = record.value();
            assertThat(payload).contains("POINTS_DEDUCT_PENDING");
            SagaMessage publishedMessage = objectMapper.readValue(payload, SagaMessage.class);
            assertThat(publishedMessage.getStep()).isEqualTo(SagaStep.POINTS_DEDUCT_PENDING);
        }

        @Test
        @DisplayName("포인트 복구 메시지를 발행한다")
        void dispatch_point_restore() throws JsonProcessingException {
            //given
            Long sagaId = 1L;
            String orderNo = UUID.randomUUID().toString();
            SagaMessage message = Instancio.of(SagaMessage.class)
                    .set(field("sagaId"), sagaId)
                    .set(field("orderNo"), orderNo)
                    .set(field("status"), SagaStatus.COMPENSATING)
                    .set(field("step"), SagaStep.POINTS_RESTORE_PENDING)
                    .create();
            //when
            dispatcher.dispatch(message);
            //then
            ConsumerRecord<String, String> record = getRecord(USER_TOPIC, orderNo);
            Header sagaIdHeader = record.headers().lastHeader("X-SAGA-ID");
            assertThat(record).isNotNull();
            assertThat(record.key()).isEqualTo(orderNo);
            assertThat(sagaIdHeader).isNotNull();
            String payload = record.value();
            assertThat(payload).contains("POINTS_RESTORE_PENDING");
            SagaMessage publishedMessage = objectMapper.readValue(payload, SagaMessage.class);
            assertThat(publishedMessage.getStep()).isEqualTo(SagaStep.POINTS_RESTORE_PENDING);
        }
    }

    private ConsumerRecord<String, String> getRecord(String topic, String orderNo) {
        ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(5));
        for (ConsumerRecord<String, String> record : records.records(topic)) {
            if(orderNo.equals(record.key())) {
                return record;
            }
        }
        throw new IllegalArgumentException("토픽에서 orderNo 메시지를 찾을 수 없음");
    }
}