package com.example.order_service.saga.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrderSaga is a Querydsl query type for OrderSaga
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOrderSaga extends EntityPathBase<OrderSaga> {

    private static final long serialVersionUID = 593382839L;

    public static final QOrderSaga orderSaga = new QOrderSaga("orderSaga");

    public final com.example.order_service.common.entity.QBaseAggregateRoot _super = new com.example.order_service.common.entity.QBaseAggregateRoot(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final EnumPath<SagaStep> currentStep = createEnum("currentStep", SagaStep.class);

    public final StringPath failureReason = createString("failureReason");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> orderId = createNumber("orderId", Long.class);

    public final ListPath<OrderSagaExecution, QOrderSagaExecution> orderSagaExecutions = this.<OrderSagaExecution, QOrderSagaExecution>createList("orderSagaExecutions", OrderSagaExecution.class, QOrderSagaExecution.class, PathInits.DIRECT2);

    public final SimplePath<OrderSagaPayload> payload = createSimple("payload", OrderSagaPayload.class);

    public final EnumPath<SagaStatus> status = createEnum("status", SagaStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> version = createNumber("version", Long.class);

    public QOrderSaga(String variable) {
        super(OrderSaga.class, forVariable(variable));
    }

    public QOrderSaga(Path<? extends OrderSaga> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOrderSaga(PathMetadata metadata) {
        super(OrderSaga.class, metadata);
    }

}

