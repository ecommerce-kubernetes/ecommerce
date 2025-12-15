package com.example.order_service.api.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Map;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(
        partitions = 1,
        brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"},
        topics = {"order.saga.inventory.deduct"}
)
public abstract class IncludeInfraTest {

    protected static final String INVENTORY_DEDUCTED_TOPIC_NAME = "order.saga.inventory.deduct";
    @Autowired
    protected EmbeddedKafkaBroker embeddedKafkaBroker;
    @Autowired
    protected ObjectMapper objectMapper;

    protected Consumer<String, String> createTestConsumer(String topicName) {
        String bootstrapServers = embeddedKafkaBroker.getBrokersAsString();
        Map<String, Object> props = KafkaTestUtils.consumerProps(bootstrapServers, "order", "true");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        ConsumerFactory<String, String> factory = new DefaultKafkaConsumerFactory<>(props);
        Consumer<String, String> consumer = factory.createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, topicName);
        return consumer;
    }
}
