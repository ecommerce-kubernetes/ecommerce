package com.example.order_service.saga.domain;

import com.example.order_service.common.entity.BaseAggregateRoot;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.common.util.OrderSagaPayloadConverter;
import com.example.order_service.saga.domain.context.CreateOrderSagaContext;
import com.example.order_service.saga.domain.event.ReduceInventoryEvent;
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

    @Convert(converter = OrderSagaPayloadConverter.class)
    @Column(columnDefinition = "TEXT")
    private OrderSagaPayload payload;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "orderSaga", cascade = CascadeType.ALL, orphanRemoval = true)
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

        orderSaga.registerEvent(ReduceInventoryEvent.builder()
                .orderId(orderSaga.getOrderId())
                .executionId(execution.getId())
                .orderLines(context.payload().orderLines())
                .build());

        return orderSaga;
    }

    public void completeForward(Long executionId, IdGenerator idGenerator) {
        OrderSagaExecution execution = getExecution(executionId);

        if (execution.getStatus() == ExecutionStatus.SUCCESS) {
            return;
        }

        execution.success();

        SagaStep nextStep = determineNextForwardStep(this.currentStep, this.payload);

        if (nextStep == SagaStep.END) {
            completeSaga();
            return;
        }

        transitToForwardStep(nextStep, idGenerator);
    }

    public void failForward(Long executionId, String failureReason, IdGenerator idGenerator) {
        OrderSagaExecution execution = getExecution(executionId);

        if (execution.getStatus() == ExecutionStatus.FAIL) {
            return;
        }

        execution.fail();

        this.failureReason = failureReason;

        OrderSagaExecution rollbackExecution = nextRollbackTarget();

        if (rollbackExecution == null) {
            abortSaga();
            return;
        }

        transitToCompensateStep(rollbackExecution.getStep(), idGenerator);
    }

    public void completeCompensate(Long executionId, IdGenerator idGenerator) {
        OrderSagaExecution execution = getExecution(executionId);

        if (execution.getStatus() == ExecutionStatus.SUCCESS) {
            return;
        }

        execution.success();

        OrderSagaExecution rollbackExecution = nextRollbackTarget();

        if (rollbackExecution == null) {
            abortSaga();
            return;
        }

        transitToCompensateStep(rollbackExecution.getStep(), idGenerator);
    }

    public void failCompensate(Long executionId) {
        OrderSagaExecution execution = getExecution(executionId);

        if (execution.getStatus() == ExecutionStatus.FAIL) {
            return;
        }

        execution.fail();

        failSaga();
    }

    public OrderSagaExecution getExecution(ExecutionStatus status, ExecutionType type, SagaStep step) {
        return this.orderSagaExecutions.stream()
                .filter(exec -> exec.getType() == type)
                .filter(exec -> exec.getStatus() == status)
                .filter(exec -> exec.getStep() == step)
                .findFirst()
                .orElseThrow(() -> new ExecutionNotFoundException("작업을 찾을 수 없습니다."));
    }

    private OrderSagaExecution getExecution(Long executionId) {
        return this.orderSagaExecutions.stream()
                .filter(execution -> execution.getId().equals(executionId))
                .findFirst()
                .orElseThrow(() -> new ExecutionNotFoundException("사가 작업을 칮을 수 없습니다 executionId: " + executionId));
    }

    private void addExecution(OrderSagaExecution execution) {
        this.orderSagaExecutions.add(execution);
        execution.setOrderSaga(this);
    }

    private SagaStep determineNextForwardStep(SagaStep currentStep, OrderSagaPayload payload) {
        return switch (currentStep) {
            case INVENTORY -> payload.hasCoupons() ? SagaStep.COUPON :
                    payload.hasPoints() ? SagaStep.POINT : SagaStep.END;

            case COUPON -> payload.hasPoints() ? SagaStep.POINT : SagaStep.END;

            case POINT, END -> SagaStep.END;
        };
    }

    private void completeSaga() {
        this.currentStep = SagaStep.END;
        this.status = SagaStatus.COMPLETE;
    }

    private void failSaga() {
        this.currentStep = SagaStep.END;
        this.status = SagaStatus.FAILED;
    }

    private void abortSaga() {
        this.currentStep = SagaStep.END;
        this.status = SagaStatus.ABORT;
    }

    private void transitToForwardStep(SagaStep nextStep, IdGenerator idGenerator) {
        OrderSagaExecution nextExecution = OrderSagaExecution.create(idGenerator, ExecutionType.FORWARD, nextStep);
        addExecution(nextExecution);
        this.currentStep = nextStep;
    }

    private void transitToCompensateStep(SagaStep nextStep, IdGenerator idGenerator) {
        OrderSagaExecution rollbackExecution = OrderSagaExecution.create(idGenerator, ExecutionType.COMPENSATE, nextStep);
        addExecution(rollbackExecution);
        this.currentStep = nextStep;
        this.status = SagaStatus.COMPENSATING;
    }

    private OrderSagaExecution nextRollbackTarget() {
        Set<SagaStep> alreadyCompensatedSteps = this.orderSagaExecutions.stream()
                .filter(exec -> exec.getType() == ExecutionType.COMPENSATE)
                .map(OrderSagaExecution::getStep)
                .collect(Collectors.toSet());

        for (int i = this.orderSagaExecutions.size() - 1; i >= 0; i--) {
            OrderSagaExecution execution = this.orderSagaExecutions.get(i);
            if (execution.getType() == ExecutionType.FORWARD &&
                    execution.getStatus() == ExecutionStatus.SUCCESS &&
                    !alreadyCompensatedSteps.contains(execution.getStep())) {
                return execution;
            }
        }

        return null;
    }
}
