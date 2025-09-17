package com.example.order_service.messaging;

import com.example.common.DeductedProduct;
import com.example.common.ItemOption;
import com.example.common.PriceInfo;
import com.example.common.ProductStockDeductedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;


@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"product.stock.deducted"})
@Testcontainers
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
    @Autowired
    RedisTemplate<String, Object> redisTemplate;
    @MockitoSpyBean
    private OrderKafkaListener orderKafkaListener;
    @Autowired
    KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
    private static final String PRODUCT_TOPIC = "product.stock.deducted";
    ObjectMapper mapper = new ObjectMapper();

    @Container
    static final GenericContainer redis = new GenericContainer(DockerImageName.parse("redis:6-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry){
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Test
    void productSagaSuccessListenerTest_deducted_success() {
        Map<String, Object> initialSagaState = new HashMap<>();

        initialSagaState.put("status", "PENDING");
        initialSagaState.put("createdAt", LocalDateTime.now().toString());
        redisTemplate.opsForHash().putAll("saga:order:1", initialSagaState);

        for (MessageListenerContainer container : kafkaListenerEndpointRegistry.getListenerContainers()){
            ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
        }

        ProductStockDeductedEvent publishedEvent = new ProductStockDeductedEvent(1L,
                List.of(new DeductedProduct(1L, 1L, "상품1", "http://product.jpg",
                        new PriceInfo(3000, 10, 300, 2700),
                        2,
                        List.of(new ItemOption("색상", "RED")))));

        kafkaTemplate.send(PRODUCT_TOPIC, publishedEvent);
        kafkaTemplate.flush();

        verify(orderKafkaListener, timeout(10000).times(1))
                .productSagaSuccessListener(any(ProductStockDeductedEvent.class));

        assertThat(redisTemplate.hasKey("saga:order:1")).isTrue();
        Object product = redisTemplate.opsForHash().get("saga:order:1", "product");
        ProductStockDeductedEvent savedEvent = mapper.convertValue(product, ProductStockDeductedEvent.class);
        assertThat(savedEvent.getOrderId()).isEqualTo(1L);
        assertThat(savedEvent.getDeductedProducts())
                .extracting(DeductedProduct::getProductId,
                        DeductedProduct::getProductVariantId,
                        DeductedProduct::getProductName,
                        DeductedProduct::getThumbnail,
                        DeductedProduct::getQuantity)
                .containsExactlyInAnyOrder(
                        tuple(1L, 1L, "상품1", "http://product.jpg",2)
                );

        assertThat(savedEvent.getDeductedProducts())
                .extracting(
                        dp -> dp.getPriceInfo().getPrice(),          // Long/BigDecimal -> int
                        dp -> dp.getPriceInfo().getDiscountRate(),
                        dp -> dp.getPriceInfo().getDiscountAmount(),
                        dp -> dp.getPriceInfo().getFinalPrice()
                )
                .containsExactlyInAnyOrder(
                        tuple(3000, 10, 300L, 2700L)
                );

    }
}