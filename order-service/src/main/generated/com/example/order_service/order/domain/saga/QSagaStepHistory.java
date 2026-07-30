package com.example.order_service.order.domain.saga;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSagaStepHistory is a Querydsl query type for SagaStepHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSagaStepHistory extends EntityPathBase<SagaStepHistory> {

    private static final long serialVersionUID = 1142578857L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSagaStepHistory sagaStepHistory = new QSagaStepHistory("sagaStepHistory");

    public final com.example.order_service.common.entity.QBaseEntity _super = new com.example.order_service.common.entity.QBaseEntity(this);

    public final StringPath code = createString("code");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<StepResult> result = createEnum("result", StepResult.class);

    public final QOrderSagaInstance saga;

    public final EnumPath<SagaStep> step = createEnum("step", SagaStep.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QSagaStepHistory(String variable) {
        this(SagaStepHistory.class, forVariable(variable), INITS);
    }

    public QSagaStepHistory(Path<? extends SagaStepHistory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSagaStepHistory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSagaStepHistory(PathMetadata metadata, PathInits inits) {
        this(SagaStepHistory.class, metadata, inits);
    }

    public QSagaStepHistory(Class<? extends SagaStepHistory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.saga = inits.isInitialized("saga") ? new QOrderSagaInstance(forProperty("saga"), inits.get("saga")) : null;
    }

}

