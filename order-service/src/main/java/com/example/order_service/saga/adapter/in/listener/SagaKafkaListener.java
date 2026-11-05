package com.example.order_service.saga.adapter.in.listener;

import com.example.order_service.saga.adapter.in.listener.dto.SagaReplyMessagePayload;
import com.example.order_service.saga.adapter.in.listener.dto.SagaReplyType;
import com.example.order_service.saga.application.service.OrderSagaCommandService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.BiConsumer;

@Component
public class SagaKafkaListener {

    private final OrderSagaCommandService orderSagaCommandService;
    private final Map<SagaReplyAction, BiConsumer<Long, SagaReplyMessagePayload>> router;

    public SagaKafkaListener(OrderSagaCommandService orderSagaCommandService) {
        this.orderSagaCommandService = orderSagaCommandService;

        this.router = Map.of(
                SagaReplyAction.FORWARD_SUCCESS, (sagaId, p) -> orderSagaCommandService.completeForward(sagaId, p.executionId()),
                SagaReplyAction.FORWARD_FAIL, (sagaId, p) -> orderSagaCommandService.failForward(sagaId, p.executionId(), p.failureReason()),
                SagaReplyAction.COMPENSATE_SUCCESS, (sagaId, p) -> orderSagaCommandService.completeCompensate(sagaId, p.executionId()),
                SagaReplyAction.COMPENSATE_FAIL, (sagaId, p) -> orderSagaCommandService.failCompensate(sagaId, p.executionId())
        );
    }

    @KafkaListener(topics = "${order.topics.order-saga-reply}", groupId = "order-saga-reply-group")
    public void handleReplyMessage(
            @Payload SagaReplyMessagePayload payload,
            @Header("X-Saga-Id") Long sagaId,
            @Header("X-Reply-Type") SagaReplyType replyType) {
        SagaReplyAction action = SagaReplyAction.route(replyType, payload.result());

        router.get(action).accept(sagaId, payload);
    }

}
