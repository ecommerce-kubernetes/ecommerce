package com.example.order_service.saga.adapter.out.message.processor;

import com.example.order_service.saga.domain.OrderSagaPayload;
import com.example.order_service.saga.domain.event.RestoreCouponEvent;
import com.example.order_service.saga.domain.event.UsedCouponEvent;
import com.example.order_service.support.annotation.MockRedis;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.Message;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = {
        "spring.cloud.bus.enabled=false",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
@EmbeddedKafka(partitions = 1, topics = { "coupon-saga-command" })
@MockRedis
class CouponMessageProcessorTest {

    @Autowired
    private CouponMessageProcessor processor;

    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Captor
    private ArgumentCaptor<Message<String>> messageCaptor;

    @Test
    @DisplayName("쿠폰 사용 이벤트 수신시 쿠폰 사용 메시지를 발행한다.")
    void process_whenReceiveUsedCouponEvent_thenPublishUsedCouponCommand() {
        //given
        UsedCouponEvent usedEvent = createUsedEvent();
        //when
        processor.process(usedEvent);
        //then
        verify(kafkaTemplate, times(1)).send(messageCaptor.capture());
        Message<String> capturedMessage = messageCaptor.getValue();

        assertThat(capturedMessage.getHeaders().get(KafkaHeaders.KEY)).isEqualTo("1");
        assertThat(capturedMessage.getHeaders().get("X-Saga-Id")).isEqualTo(1L);
        assertThat(capturedMessage.getHeaders().get("X-Event-Type")).isEqualTo("UsedCouponCommand");
        assertThat(capturedMessage.getHeaders().containsKey(KafkaHeaders.TOPIC)).isTrue();

        String payload = capturedMessage.getPayload();
        assertThat(payload).contains("\"executionId\":1");
        assertThat(payload).contains("\"userId\":1");
        assertThat(payload).contains("\"cartCouponId\":1");
        assertThat(payload).contains("\"itemCouponIds\":[2,3]");
    }

    @Test
    @DisplayName("쿠폰 복구 이벤트 수신시 쿠폰 복구 메시지를 발행한다.")
    void process_whenReceiveRestoreCouponEvent_thenPublishRestoreCouponCommand() {
        //given
        RestoreCouponEvent restoreEvent = createRestoreEvent();
        //when
        processor.process(restoreEvent);
        //then
        verify(kafkaTemplate, times(1)).send(messageCaptor.capture());
        Message<String> capturedMessage = messageCaptor.getValue();

        assertThat(capturedMessage.getHeaders().get(KafkaHeaders.KEY)).isEqualTo("1");
        assertThat(capturedMessage.getHeaders().get("X-Saga-Id")).isEqualTo(1L);
        assertThat(capturedMessage.getHeaders().get("X-Event-Type")).isEqualTo("RestoreCouponCommand");
        assertThat(capturedMessage.getHeaders().containsKey(KafkaHeaders.TOPIC)).isTrue();

        String payload = capturedMessage.getPayload();
        assertThat(payload).contains("\"executionId\":1");
        assertThat(payload).contains("\"userId\":1");
        assertThat(payload).contains("\"cartCouponId\":1");
        assertThat(payload).contains("\"itemCouponIds\":[2,3]");
    }

    private UsedCouponEvent createUsedEvent() {
        OrderSagaPayload.UsedCoupons usedCoupons = OrderSagaPayload.UsedCoupons.builder()
                .cartCouponId(1L)
                .itemCouponIds(List.of(2L, 3L))
                .build();

        return UsedCouponEvent.builder()
                .sagaId(1L)
                .orderId(1L)
                .executionId(1L)
                .userId(1L)
                .coupons(usedCoupons)
                .build();
    }

    private RestoreCouponEvent createRestoreEvent() {
        OrderSagaPayload.UsedCoupons usedCoupons = OrderSagaPayload.UsedCoupons.builder()
                .cartCouponId(1L)
                .itemCouponIds(List.of(2L, 3L))
                .build();

        return RestoreCouponEvent.builder()
                .sagaId(1L)
                .orderId(1L)
                .executionId(1L)
                .userId(1L)
                .coupons(usedCoupons)
                .build();
    }
}