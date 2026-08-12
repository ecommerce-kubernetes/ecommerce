package com.example.order_service.saga.domain;

import com.example.order_service.common.entity.BaseAggregateRoot;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.saga.domain.context.CreateOrderSagaContext;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "orderSagaExecution", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderSagaExecution> orderSagaExecutions = new ArrayList<>();

    private String failureReason;

    @Version
    private Long version;

    @Builder(access = AccessLevel.PRIVATE)
    private OrderSaga(Long id, Long orderId, SagaStatus status, SagaStep currentStep, OrderSagaPayload payload, String failureReason) {
        this.id = id;
        this.orderId = orderId;
        this.status = status;
        this.currentStep = currentStep;
        this.payload = payload;
        this.failureReason = failureReason;
    }

    public static OrderSaga create(CreateOrderSagaContext context, IdGenerator idGenerator) {
        Assert.notNull(idGenerator, "주문 사가 생성시 아이디 생성기는 필수이다.");

        Long id = idGenerator.generate();

        Assert.notNull(id, "주문 사가 생성시 아이디는 필수이다.");

        OrderSaga orderSaga = OrderSaga.builder()
                .id(id)
                .orderId(context.orderId())
                .status(SagaStatus.PROCESSING)
                .currentStep(SagaStep.INVENTORY)
                .payload(context.payload())
                .build();

        OrderSagaExecution execution = OrderSagaExecution.create(idGenerator, SagaStep.INVENTORY);
        orderSaga.addExecution(execution);

        return orderSaga;
    }

    private void addExecution(OrderSagaExecution execution) {
        this.orderSagaExecutions.add(execution);
        execution.setOrderSaga(this);
    }
}
