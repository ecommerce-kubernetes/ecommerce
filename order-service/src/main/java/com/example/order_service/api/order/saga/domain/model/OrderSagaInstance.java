package com.example.order_service.api.order.saga.domain.model;

import com.example.order_service.api.order.saga.domain.model.vo.Payload;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderSagaInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long orderId;
    @Enumerated(EnumType.STRING)
    private SagaStatus sagaStatus;
    @Enumerated(EnumType.STRING)
    private SagaStep sagaStep;
    @Column(name = "payload", columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private Payload payload;
    private String failureReason;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private OrderSagaInstance(Long orderId,SagaStatus sagaStatus, SagaStep sagaStep, Payload payload, String failureReason,
                              LocalDateTime startedAt, LocalDateTime finishedAt) {
        this.orderId = orderId;
        this.sagaStatus = sagaStatus;
        this.sagaStep = sagaStep;
        this.payload = payload;
        this.failureReason = failureReason;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
    }

    public static OrderSagaInstance start(Long orderId, Payload payload) {
        return of(orderId, SagaStatus.STARTED, SagaStep.PRODUCT, payload, null, LocalDateTime.now(), null);
    }

    private static OrderSagaInstance of(Long orderId, SagaStatus sagaStatus, SagaStep sagaStep, Payload payload, String failureReason,
                                        LocalDateTime startedAt, LocalDateTime finishedAt) {
        return OrderSagaInstance.builder()
                .orderId(orderId)
                .sagaStatus(sagaStatus)
                .sagaStep(sagaStep)
                .payload(payload)
                .failureReason(failureReason)
                .startedAt(startedAt)
                .finishedAt(finishedAt)
                .build();
    }
}
