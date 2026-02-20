package com.example.order_service.api.order.saga.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QOrderSagaInstance is a Querydsl query type for OrderSagaInstance
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOrderSagaInstance extends EntityPathBase<OrderSagaInstance> {

    private static final long serialVersionUID = -1222376589L;

    public static final QOrderSagaInstance orderSagaInstance = new QOrderSagaInstance("orderSagaInstance");

    public final StringPath failureReason = createString("failureReason");

    public final DateTimePath<java.time.LocalDateTime> finishedAt = createDateTime("finishedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath orderNo = createString("orderNo");

    public final SimplePath<com.example.order_service.api.order.saga.domain.model.vo.Payload> payload = createSimple("payload", com.example.order_service.api.order.saga.domain.model.vo.Payload.class);

    public final EnumPath<SagaStatus> sagaStatus = createEnum("sagaStatus", SagaStatus.class);

    public final EnumPath<SagaStep> sagaStep = createEnum("sagaStep", SagaStep.class);

    public final DateTimePath<java.time.LocalDateTime> startedAt = createDateTime("startedAt", java.time.LocalDateTime.class);

    public QOrderSagaInstance(String variable) {
        super(OrderSagaInstance.class, forVariable(variable));
    }

    public QOrderSagaInstance(Path<? extends OrderSagaInstance> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOrderSagaInstance(PathMetadata metadata) {
        super(OrderSagaInstance.class, metadata);
    }

}

