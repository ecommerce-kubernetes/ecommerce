package com.example.order_service.order.domain.saga;

import com.example.order_service.common.entity.BaseEntity;
import com.example.order_service.order.domain.vo.SagaPayload;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderSagaInstance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String orderNo;
    @Enumerated(EnumType.STRING)
    private SagaStep currentStep;
    @Enumerated(EnumType.STRING)
    private SagaStatus status;
    @Embedded
    private SagaPayload payload;
    @Version
    private Long version;

    @OneToMany(mappedBy = "saga", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SagaStepHistory> histories = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private OrderSagaInstance(String orderNo, SagaStep currentStep, SagaStatus status, SagaPayload payload) {
        this.orderNo = orderNo;
        this.currentStep = currentStep;
        this.status = status;
        this.payload = payload;
    }

    public static OrderSagaInstance create(String orderNo, SagaStep currentStep, SagaPayload payload) {
        return OrderSagaInstance.builder()
                .orderNo(orderNo)
                .currentStep(currentStep)
                .status(SagaStatus.STARTED)
                .payload(payload)
                .build();
    }

    public void addHistory(SagaStepHistory history) {
        histories.add(history);
        history.setSaga(this);
    }

    public void transitionTo(SagaStep nextStep) {
        this.currentStep = nextStep;
        if (nextStep.isCompensation() && this.status != SagaStatus.COMPENSATING) {
            this.status = SagaStatus.COMPENSATING;
        }
    }

    public void complete(){
        this.status = SagaStatus.COMPLETE;
        this.currentStep = SagaStep.END;
    }

    public void failed() {
        this.status = SagaStatus.FAILED;
    }

    public String getCauseCode() {
        return this.histories.stream()
                .filter(h -> h.getResult() == StepResult.FAILED)
                .min(Comparator.comparing(SagaStepHistory::getId))
                .map(SagaStepHistory::getCode)
                .orElse("UNKNOWN_SAGA_ERROR");
    }
}
