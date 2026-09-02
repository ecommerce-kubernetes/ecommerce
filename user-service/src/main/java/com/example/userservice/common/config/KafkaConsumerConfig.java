package com.example.userservice.common.config;

import com.example.userservice.common.exception.BusinessException;
import com.example.userservice.user.adapter.in.listener.dto.PointSagaCommand;
import com.example.userservice.user.adapter.in.listener.dto.PointSagaCommandPayload;
import com.example.userservice.user.application.service.PointSagaProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.util.backoff.FixedBackOff;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    private final PointSagaProcessor processor;

    private final ObjectMapper objectMapper;

    private RecordMessageConverter recordMessageConverter() {
        return new StringJsonMessageConverter();
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Object, Object> sagaKafkaListenerContainerFactory(
            ConsumerFactory<Object, Object> consumerFactory,
            @Value("${spring.kafka.listener.auto-startup:true}") boolean autoStartup) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(sagaErrorHandler());
        factory.setRecordMessageConverter(recordMessageConverter());
        factory.setAutoStartup(autoStartup);
        return factory;
    }

    private DefaultErrorHandler sagaErrorHandler() {
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(createSagaRecoverer(), new FixedBackOff(1000L, 3));
        errorHandler.addNotRetryableExceptions(BusinessException.class);
        errorHandler.addNotRetryableExceptions(IllegalArgumentException.class);
        errorHandler.addNotRetryableExceptions(UnsupportedOperationException.class);
        return errorHandler;
    }

    private ConsumerRecordRecoverer createSagaRecoverer() {
        return (record, exception) -> {
            try {
                Long sagaId = Long.parseLong((String) record.key());

                byte[] commandTypeBytes = record.headers().lastHeader("X-Command-Type").value();
                PointSagaCommand command = PointSagaCommand.valueOf(new String(commandTypeBytes));

                String payloadJson = (String) record.value();
                PointSagaCommandPayload payload = objectMapper.readValue(payloadJson, PointSagaCommandPayload.class);
                Long executionId = payload.executionId();

                String failureReason = "SYSTEM_RETRY_EXHAUSTED";

                switch (command) {
                    case RESTORE_POINT -> processor.failRefund(sagaId, executionId, failureReason);
                    case USE_POINT -> processor.failDeduct(sagaId, executionId, failureReason);
                }

            } catch (Exception e) {
                log.error("Recovery Fallback Failed for record: {}", record, e);
            }
        };
    }
}
