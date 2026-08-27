package com.example.order_service.saga.domain.tmp;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSagaPayloadDeprecated_CouponPayload is a Querydsl query type for CouponPayload
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QSagaPayloadDeprecated_CouponPayload extends BeanPath<SagaPayloadDeprecated.CouponPayload> {

    private static final long serialVersionUID = 1802440925L;

    public static final QSagaPayloadDeprecated_CouponPayload couponPayload = new QSagaPayloadDeprecated_CouponPayload("couponPayload");

    public final NumberPath<Long> cartCouponId = createNumber("cartCouponId", Long.class);

    public final ListPath<Long, NumberPath<Long>> itemCouponIds = this.<Long, NumberPath<Long>>createList("itemCouponIds", Long.class, NumberPath.class, PathInits.DIRECT2);

    public QSagaPayloadDeprecated_CouponPayload(String variable) {
        super(SagaPayloadDeprecated.CouponPayload.class, forVariable(variable));
    }

    public QSagaPayloadDeprecated_CouponPayload(Path<? extends SagaPayloadDeprecated.CouponPayload> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSagaPayloadDeprecated_CouponPayload(PathMetadata metadata) {
        super(SagaPayloadDeprecated.CouponPayload.class, metadata);
    }

}

