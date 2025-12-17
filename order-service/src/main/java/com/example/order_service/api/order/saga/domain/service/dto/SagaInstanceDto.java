package com.example.order_service.api.order.saga.domain.service.dto;

import com.example.order_service.api.order.saga.domain.model.OrderSagaInstance;
import com.example.order_service.api.order.saga.domain.model.SagaProgress;
import com.example.order_service.api.order.saga.domain.model.SagaStep;
import com.example.order_service.api.order.saga.domain.model.vo.Payload;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SagaInstanceDto {
    private Long id;
    private Long orderId;
    private SagaStep sagaStep;
    private SagaProgress sagaProgress;
    private Payload payload;
    private String failureReason;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

    @Builder
    private SagaInstanceDto(Long id, Long orderId, SagaStep sagaStep, SagaProgress sagaProgress, Payload payload, String failureReason, LocalDateTime startedAt, LocalDateTime finishedAt) {
        this.id = id;
        this.orderId = orderId;
        this.sagaStep = sagaStep;
        this.sagaProgress = sagaProgress;
        this.payload = payload;
        this.failureReason = failureReason;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
    }

    public static SagaInstanceDto from(OrderSagaInstance orderSagaInstance){
        return SagaInstanceDto.builder()
                .id(orderSagaInstance.getId())
                .orderId(orderSagaInstance.getOrderId())
                .sagaStep(orderSagaInstance.getSagaStep())
                .sagaProgress(orderSagaInstance.getSagaProgress())
                .payload(orderSagaInstance.getPayload())
                .failureReason(orderSagaInstance.getFailureReason())
                .startedAt(orderSagaInstance.getStartedAt())
                .finishedAt(orderSagaInstance.getFinishedAt())
                .build();
    }
}
