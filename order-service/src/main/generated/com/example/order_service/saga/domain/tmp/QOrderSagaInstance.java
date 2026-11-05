package com.example.order_service.saga.domain.tmp;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrderSagaInstance is a Querydsl query type for OrderSagaInstance
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOrderSagaInstance extends EntityPathBase<OrderSagaInstance> {

    private static final long serialVersionUID = -1824898603L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOrderSagaInstance orderSagaInstance = new QOrderSagaInstance("orderSagaInstance");

    public final com.example.order_service.common.entity.QBaseEntity _super = new com.example.order_service.common.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final EnumPath<com.example.order_service.saga.domain.SagaStep> currentStep = createEnum("currentStep", com.example.order_service.saga.domain.SagaStep.class);

    public final ListPath<SagaStepHistory, QSagaStepHistory> histories = this.<SagaStepHistory, QSagaStepHistory>createList("histories", SagaStepHistory.class, QSagaStepHistory.class, PathInits.DIRECT2);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath orderNo = createString("orderNo");

    public final QSagaPayloadDeprecated payload;

    public final NumberPath<Long> paymentId = createNumber("paymentId", Long.class);

    public final EnumPath<com.example.order_service.saga.domain.SagaStatus> status = createEnum("status", com.example.order_service.saga.domain.SagaStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> version = createNumber("version", Long.class);

    public QOrderSagaInstance(String variable) {
        this(OrderSagaInstance.class, forVariable(variable), INITS);
    }

    public QOrderSagaInstance(Path<? extends OrderSagaInstance> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOrderSagaInstance(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOrderSagaInstance(PathMetadata metadata, PathInits inits) {
        this(OrderSagaInstance.class, metadata, inits);
    }

    public QOrderSagaInstance(Class<? extends OrderSagaInstance> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.payload = inits.isInitialized("payload") ? new QSagaPayloadDeprecated(forProperty("payload"), inits.get("payload")) : null;
    }

}

