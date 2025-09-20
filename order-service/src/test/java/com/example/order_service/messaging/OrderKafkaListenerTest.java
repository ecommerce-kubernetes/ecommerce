package com.example.order_service.messaging;

import com.example.common.DeductedProduct;
import com.example.common.ItemOption;
import com.example.common.PriceInfo;
import com.example.common.ProductStockDeductedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;


@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"product.stock.deducted"})
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.group-id=products-test",
        "spring.kafka.consumer.auto-offset-reset=earliest"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class OrderKafkaListenerTest {
    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;
    @MockitoSpyBean
    private OrderKafkaListener orderKafkaListener;
    @Autowired
    KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
    private static final String PRODUCT_TOPIC = "product.stock.deducted";

}