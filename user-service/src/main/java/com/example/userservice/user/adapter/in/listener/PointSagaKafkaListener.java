package com.example.userservice.user.adapter.in.listener;

import com.example.userservice.common.domain.vo.Money;
import com.example.userservice.common.exception.BusinessException;
import com.example.userservice.user.adapter.in.listener.dto.PointSagaCommand;
import com.example.userservice.user.adapter.in.listener.dto.PointSagaCommandPayload;
import com.example.userservice.user.application.service.PointSagaProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PointSagaKafkaListener {

    private final PointSagaProcessor processor;

    @KafkaListener(topics = "${user.topics.saga.order.command}", groupId = "user-service-point-saga-group")
    public void handlePointMessage(@Payload PointSagaCommandPayload payload,
                                    @Header(KafkaHeaders.RECEIVED_KEY) Long sagaId,
                                    @Header("X-Command-Type") PointSagaCommand commandType) {
        Money amount = Money.wons(payload.usedPoints());

        try {
            switch (commandType) {
                case USE_POINT -> processor.deduct(sagaId, payload.executionId(), payload.userId(), amount);
                case RESTORE_POINT -> processor.refund(sagaId, payload.executionId(), payload.userId(), amount);
            }
        } catch (BusinessException e) {
            switch (commandType) {
                case USE_POINT -> processor.failDeduct(sagaId, payload.executionId(), e.getErrorCode().name());
                case RESTORE_POINT -> processor.failRefund(sagaId, payload.executionId(), e.getErrorCode().name());
            }
        }
    }
}
