package com.example.order_service.saga.domain.tmp;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSagaPayloadDeprecated is a Querydsl query type for SagaPayloadDeprecated
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QSagaPayloadDeprecated extends BeanPath<SagaPayloadDeprecated> {

    private static final long serialVersionUID = -113011421L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSagaPayloadDeprecated sagaPayloadDeprecated = new QSagaPayloadDeprecated("sagaPayloadDeprecated");

    public final QSagaPayloadDeprecated_CouponPayload coupon;

    public final ListPath<SagaPayloadDeprecated.ItemPayload, SimplePath<SagaPayloadDeprecated.ItemPayload>> items = this.<SagaPayloadDeprecated.ItemPayload, SimplePath<SagaPayloadDeprecated.ItemPayload>>createList("items", SagaPayloadDeprecated.ItemPayload.class, SimplePath.class, PathInits.DIRECT2);

    public final QSagaPayloadDeprecated_PointPayload points;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QSagaPayloadDeprecated(String variable) {
        this(SagaPayloadDeprecated.class, forVariable(variable), INITS);
    }

    public QSagaPayloadDeprecated(Path<? extends SagaPayloadDeprecated> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSagaPayloadDeprecated(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSagaPayloadDeprecated(PathMetadata metadata, PathInits inits) {
        this(SagaPayloadDeprecated.class, metadata, inits);
    }

    public QSagaPayloadDeprecated(Class<? extends SagaPayloadDeprecated> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.coupon = inits.isInitialized("coupon") ? new QSagaPayloadDeprecated_CouponPayload(forProperty("coupon")) : null;
        this.points = inits.isInitialized("points") ? new QSagaPayloadDeprecated_PointPayload(forProperty("points")) : null;
    }

}

