package com.example.order_service.saga.domain;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.entity.BaseAggregateRoot;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.saga.domain.context.CreateOrderSagaContext;
import com.example.order_service.saga.exception.ExecutionNotFoundException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

        OrderSagaExecution execution = OrderSagaExecution.create(idGenerator, ExecutionType.FORWARD, SagaStep.INVENTORY);
        orderSaga.addExecution(execution);

        return orderSaga;
    }

    public void completeStep(Long executionId, IdGenerator idGenerator) {
        OrderSagaExecution execution = getExecution(executionId);

        execution.success();

        nextStep(idGenerator);
    }

    public void failStep(Long executionId, String failureReason, IdGenerator idGenerator) {
        OrderSagaExecution execution = getExecution(executionId);

        execution.fail();
    }

    private OrderSagaExecution getExecution(Long executionId) {
        return this.orderSagaExecutions.stream()
                .filter(execution -> execution.getId().equals(executionId))
                .findFirst()
                .orElseThrow(() -> new ExecutionNotFoundException("사가 작업을 칮을 수 없습니다 executionId: " + executionId));
    }

    private void nextStep(IdGenerator idGenerator) {
        if (this.currentStep.equals(SagaStep.INVENTORY)) {
            if (payload.hasCoupons()) {
                OrderSagaExecution execution = OrderSagaExecution.create(idGenerator, ExecutionType.FORWARD, SagaStep.COUPON);
                addExecution(execution);

                this.currentStep = SagaStep.COUPON;
            } else {
                if (!payload.usedPoints().equals(Money.ZERO)) {
                    OrderSagaExecution execution = OrderSagaExecution.create(idGenerator, ExecutionType.FORWARD, SagaStep.POINT);
                    addExecution(execution);

                    this.currentStep = SagaStep.POINT;
                } else {
                    this.status = SagaStatus.COMPLETE;
                }
            }
        } else if (this.currentStep.equals(SagaStep.COUPON)) {
            if (!payload.usedPoints().equals(Money.ZERO)) {
                OrderSagaExecution execution = OrderSagaExecution.create(idGenerator, ExecutionType.FORWARD, SagaStep.POINT);
                addExecution(execution);

                this.currentStep = SagaStep.POINT;
            } else {
                this.status = SagaStatus.COMPLETE;
            }
        } else if (this.currentStep.equals(SagaStep.POINT)) {
            this.status = SagaStatus.COMPLETE;
        }
    }

    private void nextCompensate(IdGenerator idGenerator) {
        Set<SagaStep> alreadyCompensatedSteps = this.orderSagaExecutions.stream()
                .filter(exec -> exec.getType() == ExecutionType.COMPENSATE)
                .map(OrderSagaExecution::getStep)
                .collect(Collectors.toSet());

        OrderSagaExecution target = null;
        for (int i = this.orderSagaExecutions.size() - 1; i >= 0; i--) {
            OrderSagaExecution exec = this.orderSagaExecutions.get(i);
            if (exec.getType() == ExecutionType.FORWARD &&
                    exec.getStatus() == ExecutionStatus.SUCCESS &&
                    !alreadyCompensatedSteps.contains(exec.getStep())) {
                target = exec;
            }
        }

        if (target != null) {
            OrderSagaExecution execution = OrderSagaExecution.create(idGenerator, ExecutionType.COMPENSATE, SagaStep.POINT);
            addExecution(execution);
        } else {
            this.status = SagaStatus.FAILED;
        }
    }

    private void addExecution(OrderSagaExecution execution) {
        this.orderSagaExecutions.add(execution);
        execution.setOrderSaga(this);
    }
}
