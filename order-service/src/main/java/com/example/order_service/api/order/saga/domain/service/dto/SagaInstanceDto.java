package com.example.order_service.api.order.saga.domain.service.dto;

import com.example.order_service.api.order.saga.domain.model.OrderSagaInstance;
import com.example.order_service.api.order.saga.domain.model.vo.Payload;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SagaInstanceDto {
    private Long id;
    private Long orderId;
    private String step;
    private String progress;
    private Payload payload;
    private String failureReason;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

    @Builder
    private SagaInstanceDto(Long id, Long orderId, String step, String progress, Payload payload, String failureReason, LocalDateTime startedAt, LocalDateTime finishedAt) {
        this.id = id;
        this.orderId = orderId;
        this.step = step;
        this.progress = progress;
        this.payload = payload;
        this.failureReason = failureReason;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
    }

    public static SagaInstanceDto from(OrderSagaInstance orderSagaInstance){
        return SagaInstanceDto.builder()
                .id(orderSagaInstance.getId())
                .orderId(orderSagaInstance.getOrderId())
                .step(orderSagaInstance.getStep().name())
                .progress(orderSagaInstance.getProgress().name())
                .payload(orderSagaInstance.getPayload())
                .failureReason(orderSagaInstance.getFailureReason())
                .startedAt(orderSagaInstance.getStartedAt())
                .finishedAt(orderSagaInstance.getFinishedAt())
                .build();
    }
}
