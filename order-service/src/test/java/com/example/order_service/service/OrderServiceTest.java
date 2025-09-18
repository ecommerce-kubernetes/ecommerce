package com.example.order_service.service;

import com.example.common.OrderCreatedEvent;
import com.example.common.OrderProduct;
import com.example.order_service.dto.request.OrderItemRequest;
import com.example.order_service.dto.request.OrderRequest;
import com.example.order_service.dto.response.CreateOrderResponse;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SpringBootTest
@Testcontainers
@EmbeddedKafka(partitions = 1, topics = {"order.created"})
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.group-id=products-test",
        "spring.kafka.consumer.auto-offset-reset=earliest"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class OrderServiceTest {

    @Autowired
    OrderService orderService;
    @Autowired
    EmbeddedKafkaBroker embeddedKafka; // Embedded broker 인스턴스

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;


    @Container
    static final GenericContainer redis = new GenericContainer(DockerImageName.parse("redis:6-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry){
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Test
    @DisplayName("주문 생성 테스트")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void saveOrderTest_integration(){
        Map<String, Object> customerProps = KafkaTestUtils.consumerProps("test-group", "true", embeddedKafka);

        DefaultKafkaConsumerFactory<String, OrderCreatedEvent> factory = new DefaultKafkaConsumerFactory<>(
                customerProps,
                new StringDeserializer(),
                new JsonDeserializer<>(OrderCreatedEvent.class, false)
        );
        Consumer<String, OrderCreatedEvent> consumer = factory.createConsumer();
        consumer.subscribe(List.of("order.created"));
        OrderRequest request = new OrderRequest(
                List.of(new OrderItemRequest(1L, 2)),
                "서울시 테헤란로 123",
                1L,
                5000,
                400
        );
        CreateOrderResponse response = orderService.saveOrder(1L, request);

        assertThat(response.getOrderId()).isNotNull();
        assertThat(response.getSubscribeUrl()).isEqualTo("http://test.com/" + response.getOrderId() + "/subscribe");

        Boolean hashKey = redisTemplate.hasKey("saga:order:" + response.getOrderId());
        String status = (String) redisTemplate.opsForHash().get("saga:order:" + response.getOrderId(), "status");
        assertThat(hashKey).isTrue();
        assertThat(status).isEqualTo("PENDING");

        ConsumerRecord<String, OrderCreatedEvent> record = KafkaTestUtils.getSingleRecord(consumer, "order.created", Duration.ofSeconds(10));

        assertThat(record).isNotNull();
        OrderCreatedEvent eventPayload = record.value();
        assertThat(eventPayload.getOrderId()).isEqualTo(response.getOrderId());
        assertThat(eventPayload.getOrderProductList())
                .extracting(OrderProduct::getProductVariantId, OrderProduct::getStock)
                        .containsExactlyInAnyOrder(
                                tuple(1L, 2)
                        );

        assertThat(eventPayload.getUserCouponId()).isEqualTo(1L);
        assertThat(eventPayload.getReservedCashAmount()).isEqualTo(5000);
        assertThat(eventPayload.getReservedPointAmount()).isEqualTo(400);
        consumer.close();
    }
}