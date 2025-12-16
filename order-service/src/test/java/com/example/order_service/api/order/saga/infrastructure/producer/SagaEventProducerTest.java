package com.example.order_service.api.order.saga.infrastructure.producer;

import com.example.common.CouponUseRequest;
import com.example.common.InventoryDeductRequest;
import com.example.common.UserPointUseRequest;
import com.example.order_service.api.order.saga.domain.model.vo.Payload;
import com.example.order_service.api.order.saga.infrastructure.kafka.producer.SagaEventProducer;
import com.example.order_service.api.support.IncludeInfraTest;
import com.fasterxml.jackson.core.JsonProcessingException;
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
        Long orderId = 1L;
        Payload payload = Payload.builder()
                .userId(1L)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .build();

        Consumer<String, String> consumer = createTestConsumer(INVENTORY_DEDUCTED_TOPIC_NAME);
        //when
        sagaEventProducer.requestInventoryDeduction(sagaId, orderId, payload);
        //then
        ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumer, INVENTORY_DEDUCTED_TOPIC_NAME, Duration.ofMillis(5000L));
        assertThat(record.key()).isEqualTo(String.valueOf(sagaId));
        InventoryDeductRequest message = objectMapper.readValue(record.value(), InventoryDeductRequest.class);
        assertThat(message)
                .extracting("sagaId", "orderId", "userId")
                        .containsExactly(1L, 1L, 1L);
        assertThat(message.getItems()).hasSize(1);
        assertThat(message.getItems())
                .extracting("productVariantId", "quantity")
                .containsExactlyInAnyOrder(
                        tuple(1L, 3)
                );
        assertThat(message.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("쿠폰 사용 요청 메시지를 발행한다")
    void requestCouponUse() throws JsonProcessingException {
        //given
        Long sagaId = 1L;
        Long orderId = 1L;
        Payload payload = Payload.builder()
                .userId(1L)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .couponId(1L)
                .build();
        Consumer<String, String> consumer = createTestConsumer(COUPON_USED_TOPIC_NAME);
        //when
        sagaEventProducer.requestCouponUse(sagaId, orderId, payload);
        //then
        ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumer, COUPON_USED_TOPIC_NAME, Duration.ofMillis(5000L));
        assertThat(record.key()).isEqualTo(String.valueOf(sagaId));
        CouponUseRequest message = objectMapper.readValue(record.value(), CouponUseRequest.class);
        assertThat(message)
                .extracting("sagaId", "orderId", "userId", "couponId")
                .containsExactly(1L, 1L, 1L, 1L);
        assertThat(message.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("포인트 차감 메시지를 발행한다")
    void requestPointUse() throws JsonProcessingException {
        //given
        Long sagaId = 1L;
        Long orderId = 1L;
        Payload payload = Payload.builder()
                .userId(1L)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .couponId(1L)
                .useToPoint(1000L)
                .build();
        Consumer<String, String> consumer = createTestConsumer(POINT_USED_TOPIC_NAME);
        //when
        sagaEventProducer.requestUserPointUse(sagaId, orderId, payload);
        //then
        ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumer, POINT_USED_TOPIC_NAME, Duration.ofMillis(5000L));
        assertThat(record.key()).isEqualTo(String.valueOf(sagaId));
        UserPointUseRequest message = objectMapper.readValue(record.value(), UserPointUseRequest.class);
        assertThat(message)
                .extracting("sagaId", "orderId", "userId", "usedPoint", "reason")
                .containsExactly(1L, 1L, 1L, 1000L, "ORDER_DISCOUNT");
    }
}
