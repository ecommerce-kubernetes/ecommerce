package com.example.order_service.api.order.saga.infrastructure.producer;

import com.example.common.coupon.CouponCommandType;
import com.example.common.coupon.CouponSagaCommand;
import com.example.common.product.Item;
import com.example.common.product.ProductCommandType;
import com.example.common.product.ProductSagaCommand;
import com.example.common.user.UserCommandType;
import com.example.common.user.UserSagaCommand;
import com.example.order_service.api.order.saga.domain.model.vo.Payload;
import com.example.order_service.api.order.saga.infrastructure.kafka.producer.SagaEventProducer;
import com.example.order_service.api.support.IncludeInfraTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class SagaEventProducerTest extends IncludeInfraTest {

    @Autowired
    private SagaEventProducer sagaEventProducer;

    public static final String ORDER_NO = "ORD-20260101-AB12FVC";
    @Test
    @DisplayName("재고 차감 요청 메시지를 발행한다")
    void requestInventoryDeduction() throws IOException {
        //given
        Long sagaId = System.nanoTime();
        Long orderId = 1L;
        Payload payload = Payload.builder()
                .userId(1L)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .build();

        Consumer<String, String> consumer = createTestConsumer(PRODUCT_REQUEST_TOPIC_NAME);
        //when
        sagaEventProducer.requestInventoryDeduction(sagaId, ORDER_NO, payload);
        //then
        ConsumerRecord<String, String> record = getRecordByKey(consumer, PRODUCT_REQUEST_TOPIC_NAME, String.valueOf(sagaId));
        assertThat(record.key()).isEqualTo(String.valueOf(sagaId));
        ProductSagaCommand message = objectMapper.readValue(record.value(), ProductSagaCommand.class);
        assertThat(message)
                .extracting(ProductSagaCommand::getType, ProductSagaCommand::getSagaId, ProductSagaCommand::getOrderNo, ProductSagaCommand::getUserId)
                        .containsExactly(ProductCommandType.DEDUCT_STOCK, sagaId, ORDER_NO, 1L);
        assertThat(message.getItems()).hasSize(1);
        assertThat(message.getItems())
                .extracting(Item::getProductVariantId, Item::getQuantity)
                .containsExactlyInAnyOrder(
                        tuple(1L, 3)
                );
        assertThat(message.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("재고 복구 메시지를 발행한다")
    void requestInventoryCompensate() throws JsonProcessingException {
        //given
        Long sagaId = System.nanoTime();
        Long orderId = 1L;
        Payload payload = Payload.builder()
                .userId(1L)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .build();

        Consumer<String, String> consumer = createTestConsumer(PRODUCT_REQUEST_TOPIC_NAME);
        //when
        sagaEventProducer.requestInventoryCompensate(sagaId, ORDER_NO, payload);
        //then
        ConsumerRecord<String, String> record = getRecordByKey(consumer, PRODUCT_REQUEST_TOPIC_NAME, String.valueOf(sagaId));
        assertThat(record.key()).isEqualTo(String.valueOf(sagaId));
        ProductSagaCommand message = objectMapper.readValue(record.value(), ProductSagaCommand.class);
        assertThat(message)
                .extracting(ProductSagaCommand::getType, ProductSagaCommand::getSagaId, ProductSagaCommand::getOrderNo, ProductSagaCommand::getUserId)
                .containsExactly(ProductCommandType.RESTORE_STOCK, sagaId, ORDER_NO, 1L);
        assertThat(message.getItems()).hasSize(1);
        assertThat(message.getItems())
                .extracting(Item::getProductVariantId, Item::getQuantity)
                .containsExactlyInAnyOrder(
                        tuple(1L, 3)
                );
        assertThat(message.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("쿠폰 사용 요청 메시지를 발행한다")
    void requestCouponUse() throws JsonProcessingException {
        //given
        Long sagaId = System.nanoTime();
        Payload payload = Payload.builder()
                .userId(1L)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .couponId(1L)
                .build();
        Consumer<String, String> consumer = createTestConsumer(COUPON_REQUEST_TOPIC_NAME);
        //when
        sagaEventProducer.requestCouponUse(sagaId, ORDER_NO, payload);
        //then
        ConsumerRecord<String, String> record = getRecordByKey(consumer, COUPON_REQUEST_TOPIC_NAME, String.valueOf(sagaId));
        assertThat(record.key()).isEqualTo(String.valueOf(sagaId));
        CouponSagaCommand message = objectMapper.readValue(record.value(), CouponSagaCommand.class);
        assertThat(message)
                .extracting(CouponSagaCommand::getType, CouponSagaCommand::getSagaId, CouponSagaCommand::getOrderNo, CouponSagaCommand::getUserId, CouponSagaCommand::getCouponId)
                .containsExactly(CouponCommandType.USE_COUPON, sagaId, ORDER_NO, 1L, 1L);
        assertThat(message.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("쿠폰 복구 메시지를 발행한다")
    void requestCouponCompensate() throws JsonProcessingException {
        //given
        Long sagaId = System.nanoTime();
        Payload payload = Payload.builder()
                .userId(1L)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .couponId(1L)
                .build();
        Consumer<String, String> consumer = createTestConsumer(COUPON_REQUEST_TOPIC_NAME);
        //when
        sagaEventProducer.requestCouponCompensate(sagaId, ORDER_NO, payload);
        //then
        ConsumerRecord<String, String> record = getRecordByKey(consumer, COUPON_REQUEST_TOPIC_NAME, String.valueOf(sagaId));
        assertThat(record.key()).isEqualTo(String.valueOf(sagaId));
        CouponSagaCommand message = objectMapper.readValue(record.value(), CouponSagaCommand.class);
        assertThat(message)
                .extracting(CouponSagaCommand::getType, CouponSagaCommand::getSagaId, CouponSagaCommand::getOrderNo, CouponSagaCommand::getUserId, CouponSagaCommand::getCouponId)
                .containsExactly(CouponCommandType.CANCEL_USE, sagaId, ORDER_NO, 1L, 1L);
        assertThat(message.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("포인트 차감 메시지를 발행한다")
    void requestPointUse() throws JsonProcessingException {
        //given
        Long sagaId = System.nanoTime();
        Long orderId = 1L;
        Payload payload = Payload.builder()
                .userId(1L)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .couponId(1L)
                .useToPoint(1000L)
                .build();
        Consumer<String, String> consumer = createTestConsumer(USER_REQUEST_TOPIC_NAME);
        //when
        sagaEventProducer.requestUserPointUse(sagaId, ORDER_NO, payload);
        //then
        ConsumerRecord<String, String> record = getRecordByKey(consumer, USER_REQUEST_TOPIC_NAME, String.valueOf(sagaId));
        assertThat(record.key()).isEqualTo(String.valueOf(sagaId));
        UserSagaCommand message = objectMapper.readValue(record.value(), UserSagaCommand.class);
        assertThat(message)
                .extracting(UserSagaCommand::getType, UserSagaCommand::getSagaId, UserSagaCommand::getOrderNo, UserSagaCommand::getUserId, UserSagaCommand::getUsedPoint)
                .containsExactly(UserCommandType.USE_POINT, sagaId, ORDER_NO, 1L, 1000L);
    }

    @Test
    @DisplayName("포인트 복구 메시지를 발행한다")
    void requestUserPointCompensate() throws JsonProcessingException {
        //given
        Long sagaId = System.nanoTime();
        Payload payload = Payload.builder()
                .userId(1L)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .couponId(1L)
                .useToPoint(1000L)
                .build();
        Consumer<String, String> consumer = createTestConsumer(USER_REQUEST_TOPIC_NAME);
        //when
        sagaEventProducer.requestUserPointCompensate(sagaId, ORDER_NO, payload);
        //then
        ConsumerRecord<String, String> record = getRecordByKey(consumer, USER_REQUEST_TOPIC_NAME, String.valueOf(sagaId));
        assertThat(record.key()).isEqualTo(String.valueOf(sagaId));
        UserSagaCommand message = objectMapper.readValue(record.value(), UserSagaCommand.class);
        assertThat(message)
                .extracting(UserSagaCommand::getType, UserSagaCommand::getSagaId, UserSagaCommand::getOrderNo, UserSagaCommand::getUserId, UserSagaCommand::getUsedPoint)
                .containsExactly(UserCommandType.REFUND_POINT, sagaId, ORDER_NO, 1L, 1000L);
    }
}
