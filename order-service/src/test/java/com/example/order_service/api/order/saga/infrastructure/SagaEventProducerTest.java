package com.example.order_service.api.order.saga.infrastructure;

import com.example.order_service.api.order.saga.domain.model.vo.Payload;
import com.example.order_service.api.support.IncludeInfraTest;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class SagaEventProducerTest extends IncludeInfraTest {

    @Autowired
    private SagaEventProducer sagaEventProducer;

    @Test
    @DisplayName("재고 차감 요청 메시지를 발행한다")
    void requestInventoryDeduction() throws IOException {
        //given
        Long sagaId = 1L;
        Payload payload = Payload.builder()
                .userId(1L)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .build();

        Consumer<String, String> consumer = createTestConsumer(INVENTORY_DEDUCTED_TOPIC_NAME);
        //when
        sagaEventProducer.requestInventoryDeduction(sagaId, payload);
        //then
        ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumer, INVENTORY_DEDUCTED_TOPIC_NAME, Duration.ofMillis(5000L));
        assertThat(record.key()).isEqualTo(String.valueOf(sagaId));
        List<Payload.SagaItem> items = objectMapper.readValue(
                record.value(),
                new TypeReference<>() {}
        );
        assertThat(items).hasSize(1);
        assertThat(items)
                .extracting("productVariantId", "quantity")
                .containsExactlyInAnyOrder(
                        tuple(1L, 3)
                );
    }
}
