package com.example.product_service.messaging;

import com.example.common.OrderCreatedEvent;
import com.example.common.Product;
import com.example.common.ProductStockDeductedEvent;
import com.example.product_service.exception.InsufficientStockException;
import com.example.product_service.service.ProductVariantService;
import com.example.product_service.service.dto.InventoryReductionItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"order.created", "product.stock.deducted", "product.stock.failed", "product.stock.restore"})
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.group-id=products-test",
        "spring.kafka.consumer.auto-offset-reset=earliest"
})
class ProductListenerTest {

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    EmbeddedKafkaBroker embeddedKafka; // Embedded broker 인스턴스

    @MockitoSpyBean
    private ProductListener productListener;
    @Autowired
    KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @MockitoBean
    private ProductVariantService productVariantService;

    ObjectMapper mapper = new ObjectMapper();


    @Test
    void kafkaListener_publish_test() throws JsonProcessingException {
        List<InventoryReductionItem> inventoryReductionItems = List.of(new InventoryReductionItem(1L, 10000, 20, 9000));
        when(productVariantService.inventoryReductionById(anyMap()))
                .thenReturn(inventoryReductionItems);

        for (MessageListenerContainer container : kafkaListenerEndpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(container, embeddedKafka.getPartitionsPerTopic());
        }
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(1L)
                .productList(List.of(new Product(1L, 30, 9000))).build();

        kafkaTemplate.send("order.created", event);
        kafkaTemplate.flush();



        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("verify-group", "false", embeddedKafka);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        Consumer<String, String> consumer = new KafkaConsumer<>(consumerProps, new StringDeserializer(), new StringDeserializer());
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer, "product.stock.deducted");

        ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(5));
        consumer.close();

        assertThat(records.count()).as("product.stock.deducted 토픽에 메시지가 발행되었는가").isGreaterThan(0);

        String payload = records.iterator().next().value();
        JsonNode jsonNode = mapper.readTree(payload);
        assertThat(jsonNode.has("orderId")).isTrue();
        assertThat(jsonNode.get("orderId").asLong()).isEqualTo(1L);
        JsonNode productList = jsonNode.get("productList");
        List<Product> products = mapper.convertValue(
                productList,
                new TypeReference<List<Product>>() {}
        );

        assertThat(products)
                .extracting(Product::getProductVariantId, Product::getStock, Product::getDiscountPrice)
                .containsExactlyInAnyOrder(
                        tuple(1L, 20, 9000)
                );
    }

    @Test
    void kafkaListener_exception_publish() throws JsonProcessingException {
        doThrow(new InsufficientStockException("Out of Stock")).when(productVariantService).inventoryReductionById(anyMap());

        for (MessageListenerContainer container : kafkaListenerEndpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(container, embeddedKafka.getPartitionsPerTopic());
        }
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(1L)
                .productList(List.of(new Product(1L, 30, 9000))).build();

        kafkaTemplate.send("order.created", event);
        kafkaTemplate.flush();



        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("verify-group", "false", embeddedKafka);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        Consumer<String, String> consumer = new KafkaConsumer<>(consumerProps, new StringDeserializer(), new StringDeserializer());
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer, "product.stock.failed");

        ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(5));
        consumer.close();

        assertThat(records.count()).as("product.stock.failed 토픽에 메시지가 발행되었는가").isGreaterThan(0);

        String payload = records.iterator().next().value();
        JsonNode jsonNode = mapper.readTree(payload);
        assertThat(jsonNode.has("orderId")).isTrue();
        assertThat(jsonNode.get("orderId").asLong()).isEqualTo(1L);
        assertThat(jsonNode.get("reason").asText()).isEqualTo("Out of Stock");
    }

    @Test
    void kafkaListener_inventoryRestorationTest(){
        for (MessageListenerContainer container : kafkaListenerEndpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(container, embeddedKafka.getPartitionsPerTopic());
        }
        ProductStockDeductedEvent event =
                ProductStockDeductedEvent.builder().productList(List.of(new Product(1L, 10, 1000))).build();

        kafkaTemplate.send("product.stock.restore", event);
        kafkaTemplate.flush();
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(productVariantService, times(1)).inventoryRestorationById(captor.capture());
            Map<Long, Integer> received = captor.getValue();
            // 전달 맵에 기대한 키/값 존재하는지 검증
            assertThat(received).containsEntry(1L, 10);
        });
    }
}