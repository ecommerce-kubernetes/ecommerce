package com.example.userservice.user.adapter.in.listener;

import com.example.userservice.common.exception.BusinessException;
import com.example.userservice.user.adapter.in.listener.dto.PointSagaCommand;
import com.example.userservice.user.adapter.in.listener.dto.PointSagaCommandPayload;
import com.example.userservice.user.adapter.in.listener.router.PointSagaCommandRouter;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PointSagaKafkaListener {
    private final List<PointSagaCommandRouter> routers;

    @KafkaListener(topics = "${user.topics.saga.order.command}",
            groupId = "user-service-point-saga-group",
            containerFactory = "sagaKafkaListenerContainerFactory")
    public void handlePointMessage(@Payload PointSagaCommandPayload payload,
                                   @Header(KafkaHeaders.RECEIVED_KEY) Long sagaId,
                                   @Header("X-Command-Type") String commandHeader) {

        PointSagaCommand command = PointSagaCommand.valueOf(commandHeader);
        PointSagaCommandRouter router = getRouter(command);

        try {
            router.execute(sagaId, payload);
        } catch (BusinessException e) {
            router.fail(sagaId, payload, e.getErrorCode().name());
        } catch (IllegalArgumentException e) {
            router.fail(sagaId, payload, "INVALID_INPUT_VALUE");
        }
    }

    private PointSagaCommandRouter getRouter(PointSagaCommand command) {
        return routers.stream()
                .filter(s -> s.supports(command))
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationException("라우터 구현체가 없습니다:" + command));
    }

}
