package com.example.order_service.messaging;

import com.example.common.*;
import com.example.order_service.service.SagaManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = {OrderKafkaListener.class, KafkaAutoConfiguration.class})
@EmbeddedKafka(partitions = 1, topics = {
        "product.stock.deducted",
        "coupon.used.applied",
        "user.cash.deducted",
        "user.cash.failed",
        "coupon.used.failed",
        "product.stock.failed"
})
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.group-id=products-test",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer",
        "spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer",
        "spring.kafka.consumer.properties.spring.json.trusted.packages=*"
})
public class OrderKafkaListenerTest {
    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;
    @Autowired
    KafkaListenerEndpointRegistry registry;
    @MockitoBean
    SagaManager sagaManager;

    private static final String PRODUCT_SUCCESS_TOPIC = "product.stock.deducted";
    private static final String USER_CASH_TOPIC = "user.cash.deducted";
    private static final String COUPON_USED_TOPIC = "coupon.used.applied";
    private static final String PRODUCT_FAILURE_TOPIC = "product.stock.failed";
    private static final String USER_CASH_FAILURE_TOPIC = "user.cash.failed";
    @BeforeEach
    void setUp(){
        for (MessageListenerContainer container : registry.getListenerContainers()) {
            int expectedPartitionCount = container.getContainerProperties().getTopics().length * embeddedKafkaBroker.getPartitionsPerTopic();
            ContainerTestUtils.waitForAssignment(container, expectedPartitionCount);
        }
    }

    @Test
    @DisplayName("상품 재고감소 성공 메시지 도착시")
    void sagaSuccessListener_productDeducted(){
        //Kafka 메시지 발생
        ProductStockDeductedEvent event =
                new ProductStockDeductedEvent(
                        1L, List.of(new DeductedProduct(1L, 3, List.of())));
        kafkaTemplate.send(PRODUCT_SUCCESS_TOPIC, event);

        //processSagaSuccess() 메시지 호출 확인
        ArgumentCaptor<ProductStockDeductedEvent> captor = ArgumentCaptor.forClass(ProductStockDeductedEvent.class);
        verify(sagaManager, timeout(10000).times(1))
                .processSagaSuccess(captor.capture());

        ProductStockDeductedEvent receivedEvent = captor.getValue();
        assertThat(receivedEvent.getOrderId()).isEqualTo(1L);
        assertThat(receivedEvent.getDeductedProducts())
                .extracting(DeductedProduct::getProductVariantId,
                        DeductedProduct::getQuantity)
                .containsExactlyInAnyOrder(
                        tuple(1L, 3)
                );
    }

    @Test
    @DisplayName("유저 캐시 감소 성공 메시지 도착시")
    void sagaSuccessListener_userCashDeducted(){
        UserCashDeductedEvent event = new UserCashDeductedEvent(1L, 1L, true,
                200, 2500, 2700);

        kafkaTemplate.send(USER_CASH_TOPIC, event);

        //processSagaSuccess() 메시지 호출 확인
        ArgumentCaptor<UserCashDeductedEvent> captor = ArgumentCaptor.forClass(UserCashDeductedEvent.class);
        verify(sagaManager, timeout(10000).times(1))
                .processSagaSuccess(captor.capture());

        UserCashDeductedEvent receivedEvent = captor.getValue();
        assertThat(receivedEvent.getOrderId()).isEqualTo(1L);
        assertThat(receivedEvent)
                .extracting(UserCashDeductedEvent::getUserId,
                        UserCashDeductedEvent::getReservedCashAmount,
                        UserCashDeductedEvent::getReservedPointAmount,
                        UserCashDeductedEvent::getExpectTotalAmount)
                .containsExactlyInAnyOrder(
                        1L, 2500, 200, 2700
                );

    }

    @Test
    @DisplayName("쿠폰 사용 성공 메시지 도착시")
    void sagaSuccessListener_couponUsed(){
        CouponUsedSuccessEvent event = new CouponUsedSuccessEvent(1L, 1L, DiscountType.AMOUNT,
                3000, 5000, 3000);
        kafkaTemplate.send(COUPON_USED_TOPIC, event);

        ArgumentCaptor<CouponUsedSuccessEvent> captor = ArgumentCaptor.forClass(CouponUsedSuccessEvent.class);
        verify(sagaManager, timeout(10000).times(1))
                .processSagaSuccess(captor.capture());

        CouponUsedSuccessEvent receivedEvent = captor.getValue();
        assertThat(receivedEvent.getOrderId()).isEqualTo(1L);
        assertThat(receivedEvent)
                .extracting(CouponUsedSuccessEvent::getUserCouponId,
                        CouponUsedSuccessEvent::getDiscountType,
                        CouponUsedSuccessEvent::getDiscountValue,
                        CouponUsedSuccessEvent::getMinPurchaseAmount,
                        CouponUsedSuccessEvent::getMaxDiscountAmount)
                .containsExactlyInAnyOrder(
                        1L, DiscountType.AMOUNT, 3000L, 5000L, 3000L
                );
    }

    @Test
    @DisplayName("상품 재고감소 실패 메시지 도착시")
    void sagaFailureListener_productFailure(){
        FailedEvent event = new FailedEvent(1L, "out of stock");
        kafkaTemplate.send(PRODUCT_FAILURE_TOPIC, event);

        ArgumentCaptor<FailedEvent> captor = ArgumentCaptor.forClass(FailedEvent.class);
        verify(sagaManager, timeout(10000).times(1))
                .processSagaFailure(captor.capture());

        FailedEvent receivedEvent = captor.getValue();
        assertThat(receivedEvent.getOrderId()).isEqualTo(1L);
        assertThat(receivedEvent.getReason()).isEqualTo("out of stock");

    }

    @Test
    @DisplayName("유저 캐시 감소 실패 메시지 도착시")
    void sagaFailureListener_userCashFailure(){
        FailedEvent event = new FailedEvent(1L, "out of point");
        kafkaTemplate.send(USER_CASH_FAILURE_TOPIC, event);

        ArgumentCaptor<FailedEvent> captor = ArgumentCaptor.forClass(FailedEvent.class);
        verify(sagaManager, timeout(10000).times(1))
                .processSagaFailure(captor.capture());

        FailedEvent receivedEvent = captor.getValue();
        assertThat(receivedEvent.getOrderId()).isEqualTo(1L);
        assertThat(receivedEvent.getReason()).isEqualTo("out of point");
    }

    @Test
    @DisplayName("유저 캐시 감소 실패 메시지 도착시")
    void sagaFailureListener_couponFailure(){
        FailedEvent event = new FailedEvent(1L, "invalid coupon");
        kafkaTemplate.send(USER_CASH_FAILURE_TOPIC, event);

        ArgumentCaptor<FailedEvent> captor = ArgumentCaptor.forClass(FailedEvent.class);
        verify(sagaManager, timeout(10000).times(1))
                .processSagaFailure(captor.capture());

        FailedEvent receivedEvent = captor.getValue();
        assertThat(receivedEvent.getOrderId()).isEqualTo(1L);
        assertThat(receivedEvent.getReason()).isEqualTo("invalid coupon");
    }
}
