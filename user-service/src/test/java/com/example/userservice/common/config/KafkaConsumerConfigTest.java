package com.example.userservice.common.config;

import com.example.userservice.common.domain.vo.Money;
import com.example.userservice.support.annotation.MockRedis;
import com.example.userservice.user.application.service.PointSagaProcessor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;

@SpringBootTest(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.auto-offset-reset=earliest"
})
@EmbeddedKafka(partitions = 1, topics = {"user.saga.command"})
@MockRedis
class KafkaConsumerConfigTest {

    private static final String TOPIC = "user.saga.command";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @MockitoBean
    private PointSagaProcessor processor;

    @Test
    @DisplayName("일시적인 시스템 예외가 반복되면 3회 재시도 후 리커버러가 실행된다.")
    void handlePointMessage_whenSystemExceptionPersists_thenRetryThreeTimesThenRecover() {
        //given
        willThrow(new IllegalStateException("일시적인 DB 오류"))
                .given(processor).deduct(anyLong(), anyLong(), anyLong(), any(Money.class));

        String payload = "{\"executionId\":1,\"userId\":1,\"usedPoints\":1000}";
        ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, 0, "1", payload);
        record.headers().add(new RecordHeader("X-Command-Type", "USE_POINT".getBytes(StandardCharsets.UTF_8)));
        //when
        kafkaTemplate.send(record);
        //then
        then(processor).should(timeout(Duration.ofSeconds(10).toMillis()).times(4))
                .deduct(eq(1L), eq(1L), eq(1L), eq(Money.wons(1000L)));

        then(processor).should(timeout(Duration.ofSeconds(2).toMillis()))
                .failDeduct(1L, 1L, "SYSTEM_RETRY_EXHAUSTED");
    }
}
