package com.example.order_service.api.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.kafka.consumer.group-id=saga-test-${random.uuid}",
        "spring.kafka.consumer.auto-offset-reset=earliest"
})
@EmbeddedKafka(
        partitions = 1,
        brokerProperties = {"listeners=PLAINTEXT://127.0.0.1:0"},
        topics = {
                "product.saga.command", "coupon.saga.command", "user.saga.command",
                "product.saga.reply", "coupon.saga.reply", "user.saga.reply"
        }
)
public abstract class IncludeInfraTest {

    @Value("${order.topics.product-saga-command}")
    protected String PRODUCT_REQUEST_TOPIC_NAME;
    @Value("${order.topics.coupon-saga-command}")
    protected String COUPON_REQUEST_TOPIC_NAME;
    @Value("${order.topics.user-saga-command}")
    protected String USER_REQUEST_TOPIC_NAME;
    @Value("${order.topics.product-saga-reply}")
    protected String PRODUCT_RESULT_TOPIC_NAME;
    @Value("${order.topics.coupon-saga-reply}")
    protected String COUPON_RESULT_TOPIC_NAME;
    @Value("${order.topics.user-saga-reply}")
    protected String USER_RESULT_TOPIC_NAME;
    @Autowired
    protected EmbeddedKafkaBroker embeddedKafkaBroker;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @BeforeEach
    void waitForListenerAssignment() {
        for (MessageListenerContainer container : kafkaListenerEndpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
        }
    }

    private final List<Consumer<?, ?>> consumers = new ArrayList<>();

    protected Consumer<String, String> createTestConsumer(String topicName) {
        String bootstrapServers = embeddedKafkaBroker.getBrokersAsString();
        String uniqueGroupId = UUID.randomUUID().toString();
        Map<String, Object> props = KafkaTestUtils.consumerProps(bootstrapServers, uniqueGroupId, "true");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        ConsumerFactory<String, String> factory = new DefaultKafkaConsumerFactory<>(props);
        Consumer<String, String> consumer = factory.createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, topicName);
        consumers.add(consumer);
        return consumer;
    }

    protected ConsumerRecord<String, String> getRecordByKey(Consumer<String, String> consumer, String topic, String key) {
        long endTime = System.currentTimeMillis() + 5000;

        while (System.currentTimeMillis() < endTime) {
            ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer, Duration.ofMillis(100));

            if (records.isEmpty()) {
                continue;
            }

            for (ConsumerRecord<String, String> record : records) {
                if (record.key() != null && record.key().equals(key)) {
                    return record;
                }
            }
        }
        throw new IllegalStateException("해당 Key를 가진 메시지를 찾을 수 없습니다. Key: " + key + ", Topic: " + topic);
    }

    @AfterEach
    void tearDownConsumers() {
        consumers.forEach(Consumer::close);
        consumers.clear();
    }
}
