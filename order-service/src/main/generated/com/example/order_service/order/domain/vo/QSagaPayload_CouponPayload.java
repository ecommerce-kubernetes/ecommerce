package com.example.order_service.order.domain.vo;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSagaPayload_CouponPayload is a Querydsl query type for CouponPayload
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QSagaPayload_CouponPayload extends BeanPath<SagaPayload.CouponPayload> {

    private static final long serialVersionUID = -1791829286L;

    public static final QSagaPayload_CouponPayload couponPayload = new QSagaPayload_CouponPayload("couponPayload");

    public final NumberPath<Long> cartCouponId = createNumber("cartCouponId", Long.class);

    public final ListPath<Long, NumberPath<Long>> itemCouponIds = this.<Long, NumberPath<Long>>createList("itemCouponIds", Long.class, NumberPath.class, PathInits.DIRECT2);

    public QSagaPayload_CouponPayload(String variable) {
        super(SagaPayload.CouponPayload.class, forVariable(variable));
    }

    public QSagaPayload_CouponPayload(Path<? extends SagaPayload.CouponPayload> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSagaPayload_CouponPayload(PathMetadata metadata) {
        super(SagaPayload.CouponPayload.class, metadata);
    }

}

