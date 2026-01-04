package com.example.order_service.api.order.saga.domain.service.dto;

import com.example.order_service.api.order.saga.domain.model.OrderSagaInstance;
import com.example.order_service.api.order.saga.domain.model.SagaStatus;
import com.example.order_service.api.order.saga.domain.model.SagaStep;
import com.example.order_service.api.order.saga.domain.model.vo.Payload;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SagaInstanceDto {
    private Long id;
    private String orderNo;
    private SagaStep sagaStep;
    private SagaStatus sagaStatus;
    private Payload payload;
    private String failureReason;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

    @Builder
    private SagaInstanceDto(Long id, String orderNo, SagaStep sagaStep, SagaStatus sagaStatus, Payload payload, String failureReason, LocalDateTime startedAt, LocalDateTime finishedAt) {
        this.id = id;
        this.orderNo = orderNo;
        this.sagaStep = sagaStep;
        this.sagaStatus = sagaStatus;
        this.payload = payload;
        this.failureReason = failureReason;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
    }

    public static SagaInstanceDto from(OrderSagaInstance orderSagaInstance){
        return SagaInstanceDto.builder()
                .id(orderSagaInstance.getId())
                .orderNo(orderSagaInstance.getOrderNo())
                .sagaStep(orderSagaInstance.getSagaStep())
                .sagaStatus(orderSagaInstance.getSagaStatus())
                .payload(orderSagaInstance.getPayload())
                .failureReason(orderSagaInstance.getFailureReason())
                .startedAt(orderSagaInstance.getStartedAt())
                .finishedAt(orderSagaInstance.getFinishedAt())
                .build();
    }
}
