package com.example.product_service.messaging;

import com.example.common.OrderCreatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"order.created"})
@TestPropertySource(properties = {"spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.group-id=products-test"})
class ProductListenerTest {

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @MockitoSpyBean
    private ProductListener productListener;

    @Test
    void kafkaTest_run(){
        OrderCreatedEvent build = OrderCreatedEvent.builder().orderId(1L).build();

        kafkaTemplate.send("order.created", build);
        kafkaTemplate.flush();

        await().atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> verify(productListener, times(1))
                        .inventoryReductionListener(any(OrderCreatedEvent.class)));
    }



}