package com.example.product_service.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.kafka.consumer.group-id=saga-test-${random.uuid}",
        "spring.kafka.consumer.auto-offset-reset=earliest"
})
@EmbeddedKafka(
        partitions = 1,
        brokerProperties = {"listeners=PLAINTEXT://127.0.0.1:0"},
        topics = {
                "product.saga.command", "product.saga.reply"
        }
)
public abstract class IncludeInfraTest {
    @Value("${product.topics.saga-command}")
    protected String ORDER_SAGA_COMMAND_TOPIC;
    @Value("${product.topics.saga-reply}")
    protected String ORDER_SAGA_REPLY_TOPIC;

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
}
