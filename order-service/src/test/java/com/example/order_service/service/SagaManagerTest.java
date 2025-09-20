package com.example.order_service.service;

import com.example.order_service.dto.request.OrderItemRequest;
import com.example.order_service.dto.request.OrderRequest;
import com.example.order_service.entity.OrderItems;
import com.example.order_service.entity.Orders;
import com.example.order_service.service.event.PendingOrderCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics={"order.created"})
@Testcontainers
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.group-id=products-test",
        "spring.kafka.consumer.auto-offset-reset=earliest"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class SagaManagerTest {

    @Autowired
    SagaManager sagaManager;
    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;
    @Autowired
    RedisTemplate<String, Object> redisTemplate;
    @Autowired
    KafkaListenerEndpointRegistry registry;
    @Container
    static final GenericContainer redis = new GenericContainer(DockerImageName.parse("redis:6-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry){
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @BeforeEach
    void setUp(){
        for (MessageListenerContainer container : registry.getListenerContainers()) {
            int expectedPartitionCount = container.getContainerProperties().getTopics().length * embeddedKafkaBroker.getPartitionsPerTopic();
            ContainerTestUtils.waitForAssignment(container, expectedPartitionCount);
        }
    }

    @Test
    @DisplayName("주문 생성시 (PENDING)")
    void processPendingOrderSagaTest(){
        //데이터 준비
        Orders orders = new Orders(1L, "PENDING", "서울시 테헤란로 123");
        orders.addOrderItems(List.of(new OrderItems(1L, 3)));
        OrderRequest orderRequest = new OrderRequest(
                List.of(new OrderItemRequest(1L, 3)),
                "서울시 테헤란로 123", 1L, 2500,
                200);
        ReflectionTestUtils.setField(orders, "id", 1L);
        LocalDateTime createdAt = LocalDateTime.now();
        ReflectionTestUtils.setField(orders, "createAt", createdAt);
        PendingOrderCreatedEvent pendingOrderCreatedEvent = new PendingOrderCreatedEvent(OrderService.class, orders, orderRequest);

        //로직 실행
        sagaManager.processPendingOrderSaga(pendingOrderCreatedEvent);

        //결과 확인
        //1. redis Hash Data
        Map<Object, Object> entries =
                redisTemplate.opsForHash().entries("saga:order:" + 1L);
        assertThat(entries.get("status")).isEqualTo("PENDING");
        assertThat(entries.get("orderId")).isEqualTo(1);
        assertThat(entries.get("createdAt")).isEqualTo(createdAt.toString());

        //2. redis ZSet Data
        Double score = redisTemplate.opsForZSet().score("saga:timeouts", 1L);
        assertThat(score).isNotNull();
    }
}