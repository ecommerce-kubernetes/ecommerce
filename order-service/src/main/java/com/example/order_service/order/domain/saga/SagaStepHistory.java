package com.example.order_service.order.domain.saga;

import com.example.order_service.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SagaStepHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_saga_instance_id")
    private OrderSagaInstance saga;
    @Enumerated(EnumType.STRING)
    private SagaStep step;
    @Enumerated(EnumType.STRING)
    private StepResult result;
    private String code;

    @Builder(access = AccessLevel.PRIVATE)
    private SagaStepHistory(SagaStep step, StepResult result, String code) {
        this.step = step;
        this.result = result;
        this.code = code;
    }

    public static SagaStepHistory from(SagaStep step, StepResult result, String code) {
        return SagaStepHistory.builder()
                .step(step)
                .result(result)
                .code(code)
                .build();
    }

    protected void setSaga(OrderSagaInstance saga) {
        this.saga = saga;
    }
}
