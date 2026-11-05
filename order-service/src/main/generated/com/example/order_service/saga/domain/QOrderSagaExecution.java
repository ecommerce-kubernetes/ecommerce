package com.example.order_service.saga.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrderSagaExecution is a Querydsl query type for OrderSagaExecution
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOrderSagaExecution extends EntityPathBase<OrderSagaExecution> {

    private static final long serialVersionUID = -1269605599L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOrderSagaExecution orderSagaExecution = new QOrderSagaExecution("orderSagaExecution");

    public final com.example.order_service.common.entity.QBaseEntity _super = new com.example.order_service.common.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QOrderSaga orderSaga;

    public final EnumPath<ExecutionStatus> status = createEnum("status", ExecutionStatus.class);

    public final EnumPath<SagaStep> step = createEnum("step", SagaStep.class);

    public final EnumPath<ExecutionType> type = createEnum("type", ExecutionType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QOrderSagaExecution(String variable) {
        this(OrderSagaExecution.class, forVariable(variable), INITS);
    }

    public QOrderSagaExecution(Path<? extends OrderSagaExecution> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOrderSagaExecution(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOrderSagaExecution(PathMetadata metadata, PathInits inits) {
        this(OrderSagaExecution.class, metadata, inits);
    }

    public QOrderSagaExecution(Class<? extends OrderSagaExecution> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.orderSaga = inits.isInitialized("orderSaga") ? new QOrderSaga(forProperty("orderSaga")) : null;
    }

}

