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
    private String orderNo;
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
    private OrderSagaInstance(String orderNo, SagaStatus sagaStatus, SagaStep sagaStep, Payload payload, String failureReason,
                              LocalDateTime startedAt, LocalDateTime finishedAt) {
        this.orderNo = orderNo;
        this.sagaStatus = sagaStatus;
        this.sagaStep = sagaStep;
        this.payload = payload;
        this.failureReason = failureReason;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
    }

    public void changeStep(SagaStep sagaStep) {
        this.sagaStep = sagaStep;
    }

    public void changeStatus(SagaStatus sagaStatus) {
        this.sagaStatus = sagaStatus;
    }

    public void proceedTo(SagaStep sagaStep) {
        this.sagaStep = sagaStep;
        this.sagaStatus = SagaStatus.STARTED;
    }

    public void startCompensation(SagaStep nextStep, String failureReason) {
        this.sagaStatus = SagaStatus.COMPENSATING;
        this.sagaStep = nextStep;
        this.failureReason = failureReason;
    }

    public void continueCompensation(SagaStep nextSagaStep) {
        this.sagaStatus = SagaStatus.COMPENSATING;
        this.sagaStep = nextSagaStep;
    }

    public void fail(String newFailureReason) {
        this.sagaStatus = SagaStatus.FAILED;
        this.finishedAt = LocalDateTime.now();

        if (this.failureReason == null && newFailureReason != null) {
            this.failureReason = newFailureReason;
        }
    }

    public static OrderSagaInstance create(String orderNo, Payload payload, SagaStep firstStep) {
        return OrderSagaInstance.builder()
                .orderNo(orderNo)
                .sagaStatus(SagaStatus.STARTED)
                .sagaStep(firstStep)
                .payload(payload)
                .failureReason(null)
                .startedAt(LocalDateTime.now())
                .finishedAt(null)
                .build();
    }
}
