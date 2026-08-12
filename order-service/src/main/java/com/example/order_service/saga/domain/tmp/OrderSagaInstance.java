package com.example.order_service.saga.domain.tmp;

import com.example.order_service.common.entity.BaseEntity;
import com.example.order_service.saga.domain.SagaStatus;
import com.example.order_service.saga.domain.SagaStep;
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
    private Long paymentId;
    @Enumerated(EnumType.STRING)
    private SagaStep currentStep;
    @Enumerated(EnumType.STRING)
    private SagaStatus status;
    @Embedded
    private SagaPayloadDeprecated payload;
    @Version
    private Long version;

    @OneToMany(mappedBy = "saga", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SagaStepHistory> histories = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private OrderSagaInstance(String orderNo, Long paymentId, SagaStep currentStep, SagaStatus status, SagaPayloadDeprecated payload) {
        this.orderNo = orderNo;
        this.paymentId = paymentId;
        this.currentStep = currentStep;
        this.status = status;
        this.payload = payload;
    }

    public static OrderSagaInstance create(String orderNo, Long paymentId, SagaStep currentStep, SagaPayloadDeprecated payload) {
        return OrderSagaInstance.builder()
                .orderNo(orderNo)
                .paymentId(paymentId)
                .currentStep(currentStep)
                .status(SagaStatus.PROCESSING)
                .payload(payload)
                .build();
    }

    public void addHistory(SagaStepHistory history) {
        histories.add(history);
        history.setSaga(this);
    }

    public void proceedTo(SagaStep nextStep) {
        this.currentStep = nextStep;
    }

    public void compensateTo(SagaStep nextStep) {
        this.currentStep = nextStep;
        if (this.status != SagaStatus.COMPENSATING) {
            this.status = SagaStatus.COMPENSATING;
        }
    }

    public void complete(){
        this.status = SagaStatus.COMPLETE;
        this.currentStep = SagaStep.END;
    }

    public void failed() {
        this.status = SagaStatus.FAILED;
        this.currentStep = SagaStep.END;
    }

    public String getCauseCode() {
        return this.histories.stream()
                .filter(h -> h.getResult() == StepResult.FAILED)
                .min(Comparator.comparing(SagaStepHistory::getId))
                .map(SagaStepHistory::getCode)
                .orElse("UNKNOWN_SAGA_ERROR");
    }
}
