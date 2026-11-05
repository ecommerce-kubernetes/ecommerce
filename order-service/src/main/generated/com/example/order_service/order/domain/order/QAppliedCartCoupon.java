package com.example.order_service.order.domain.order;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAppliedCartCoupon is a Querydsl query type for AppliedCartCoupon
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QAppliedCartCoupon extends BeanPath<AppliedCartCoupon> {

    private static final long serialVersionUID = -2045949708L;

    public static final QAppliedCartCoupon appliedCartCoupon = new QAppliedCartCoupon("appliedCartCoupon");

    public final NumberPath<Long> cartCouponId = createNumber("cartCouponId", Long.class);

    public final StringPath name = createString("name");

    public QAppliedCartCoupon(String variable) {
        super(AppliedCartCoupon.class, forVariable(variable));
    }

    public QAppliedCartCoupon(Path<? extends AppliedCartCoupon> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAppliedCartCoupon(PathMetadata metadata) {
        super(AppliedCartCoupon.class, metadata);
    }

}

