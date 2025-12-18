package com.example.order_service.api.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.kafka.consumer.group-id=saga-test-${random.uuid}"
})
@EmbeddedKafka(
        partitions = 1,
        brokerProperties = {"listeners=PLAINTEXT://127.0.0.1:0"},
        topics = {
                "order.saga.inventory.deduct", "order.saga.coupon.used", "order.saga.point.used",
                "product.saga.result", "coupon.saga.result", "user.saga.result"
        }
)
public abstract class IncludeInfraTest {

    @Value("${order.topics.deduct-inventory}")
    protected String INVENTORY_DEDUCTED_TOPIC_NAME;
    @Value("${order.topics.used-coupon}")
    protected String COUPON_USED_TOPIC_NAME;
    @Value("${order.topics.used-point}")
    protected String POINT_USED_TOPIC_NAME;
    @Value("${order.topics.product-result}")
    protected String PRODUCT_RESULT_TOPIC_NAME;
    @Value("${order.topics.coupon-result}")
    protected String COUPON_RESULT_TOPIC_NAME;
    @Value("${order.topics.user-result}")
    protected String USER_RESULT_TOPIC_NAME;
    @Autowired
    protected EmbeddedKafkaBroker embeddedKafkaBroker;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected KafkaTemplate<String, Object> kafkaTemplate;

    private final List<Consumer<?, ?>> consumers = new ArrayList<>();

    protected Consumer<String, String> createTestConsumer(String topicName) {
        String bootstrapServers = embeddedKafkaBroker.getBrokersAsString();
        Map<String, Object> props = KafkaTestUtils.consumerProps(bootstrapServers, "order", "true");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        ConsumerFactory<String, String> factory = new DefaultKafkaConsumerFactory<>(props);
        Consumer<String, String> consumer = factory.createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, topicName);
        consumers.add(consumer);
        return consumer;
    }

    @AfterEach
    void tearDownConsumers() {
        consumers.forEach(Consumer::close);
        consumers.clear();
    }
}
