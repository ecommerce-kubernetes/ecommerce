package com.example.order_service.saga.domain;

import com.example.order_service.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderSagaExecution extends BaseEntity {

    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_saga_id")
    private OrderSaga orderSaga;

    private ExecutionStatus status;

    private ExecutionType type;

    private SagaStep step;
}
