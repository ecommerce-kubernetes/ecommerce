package com.example.order_service.saga.domain;

import com.example.order_service.common.entity.BaseAggregateRoot;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderSaga extends BaseAggregateRoot {

    @Id
    private Long id;

    private Long orderId;

    private SagaStatus status;

    private SagaStep currentStep;

    private OrderSagaPayload payload;

    private String failureReason;

    @Version
    private Long version;
}
