package com.example.order_service.order.domain.order;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAppliedItemCoupon is a Querydsl query type for AppliedItemCoupon
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QAppliedItemCoupon extends BeanPath<AppliedItemCoupon> {

    private static final long serialVersionUID = -2011216217L;

    public static final QAppliedItemCoupon appliedItemCoupon = new QAppliedItemCoupon("appliedItemCoupon");

    public final NumberPath<Long> itemCouponId = createNumber("itemCouponId", Long.class);

    public final StringPath name = createString("name");

    public QAppliedItemCoupon(String variable) {
        super(AppliedItemCoupon.class, forVariable(variable));
    }

    public QAppliedItemCoupon(Path<? extends AppliedItemCoupon> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAppliedItemCoupon(PathMetadata metadata) {
        super(AppliedItemCoupon.class, metadata);
    }

}

