package com.example.order_service.order.domain.saga;

import com.example.order_service.common.entity.BaseEntity;
import com.example.order_service.order.domain.vo.SagaPayload;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
}
