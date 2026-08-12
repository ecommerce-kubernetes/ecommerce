package com.example.order_service.saga.domain;

import com.example.order_service.common.entity.BaseEntity;
import com.example.order_service.common.util.IdGenerator;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

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

    @Builder(access = AccessLevel.PRIVATE)
    private OrderSagaExecution(Long id, ExecutionStatus status, ExecutionType type, SagaStep step) {
        this.id = id;
        this.status = status;
        this.type = type;
        this.step = step;
    }

    public static OrderSagaExecution create(IdGenerator idGenerator, SagaStep step){
        Assert.notNull(idGenerator, "주문 사가 작업 생성시 아이디 생성기는 필수이다.");
        Assert.notNull(step, "주문 사가 작업 생성시 사가 단계는 필수이다.");
        Long id = idGenerator.generate();

        Assert.notNull(id, "주문 사가 작업 생성시 아이디는 필수이다.");

        return OrderSagaExecution.builder()
                .id(id)
                .status(ExecutionStatus.PENDING)
                .type(ExecutionType.FORWARD)
                .step(step)
                .build();
    }

    void setOrderSaga(OrderSaga orderSaga){
        this.orderSaga = orderSaga;
    }
}
