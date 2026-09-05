package com.example.order_service.order.application.messaging;

import com.example.order_service.order.application.messaging.dto.SagaMessage;
import com.example.order_service.order.domain.saga.SagaStatus;
import com.example.order_service.order.domain.saga.SagaStep;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * SAGA 메시지 디스패처
 * <p>
 * SAGA 진행 메시지를 각 핸들러를 통해 보상 및 차감 실행
 * </p>
 *
 * @author 최민식
 * @since 2026. 06. 08
 */
@Component
public class SagaMessageDispatcher {
    private final Map<SagaStep, SagaMessageHandler> forwardHandlers;
    private final Map<SagaStep, SagaMessageHandler> compensationHandlers;

    public SagaMessageDispatcher(List<SagaMessageHandler> handlers) {
        this.forwardHandlers = handlers.stream()
                .collect(Collectors.toMap(SagaMessageHandler::supportsForward, Function.identity()));
        this.compensationHandlers = handlers.stream()
                .filter(handler -> handler.supportsCompensation() != null)
                .collect(Collectors.toMap(SagaMessageHandler::supportsCompensation, Function.identity()));
    }

    /**
     * SAGA 메시지 핸들러 호출
     * <p>
     * 각 Step 에 맞는 메시지 Handler 를 호출
     * </p>
     *
     * @param message SAGA 메시지
     */
    public void dispatch(SagaMessage message) {
        if (message.getStatus() == SagaStatus.COMPENSATING) {
            SagaMessageHandler compensateHandler = compensationHandlers.get(message.getStep());
            compensateHandler.compensate(message);
            return;
        }
        SagaMessageHandler handler = forwardHandlers.get(message.getStep());
        handler.forward(message);
    }
}
