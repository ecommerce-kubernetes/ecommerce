package com.example.order_service.order.domain.vo;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSagaPayload is a Querydsl query type for SagaPayload
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QSagaPayload extends BeanPath<SagaPayload> {

    private static final long serialVersionUID = 1334046176L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSagaPayload sagaPayload = new QSagaPayload("sagaPayload");

    public final QSagaPayload_CouponPayload coupon;

    public final ListPath<SagaPayload.ItemPayload, SimplePath<SagaPayload.ItemPayload>> items = this.<SagaPayload.ItemPayload, SimplePath<SagaPayload.ItemPayload>>createList("items", SagaPayload.ItemPayload.class, SimplePath.class, PathInits.DIRECT2);

    public final QSagaPayload_PointPayload points;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QSagaPayload(String variable) {
        this(SagaPayload.class, forVariable(variable), INITS);
    }

    public QSagaPayload(Path<? extends SagaPayload> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSagaPayload(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSagaPayload(PathMetadata metadata, PathInits inits) {
        this(SagaPayload.class, metadata, inits);
    }

    public QSagaPayload(Class<? extends SagaPayload> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.coupon = inits.isInitialized("coupon") ? new QSagaPayload_CouponPayload(forProperty("coupon")) : null;
        this.points = inits.isInitialized("points") ? new QSagaPayload_PointPayload(forProperty("points")) : null;
    }

}

