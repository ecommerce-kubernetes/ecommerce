package com.example.order_service.order.domain.vo;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCartCouponSnapshot is a Querydsl query type for CartCouponSnapshot
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QCartCouponSnapshot extends BeanPath<CartCouponSnapshot> {

    private static final long serialVersionUID = 103459056L;

    public static final QCartCouponSnapshot cartCouponSnapshot = new QCartCouponSnapshot("cartCouponSnapshot");

    public final NumberPath<Long> couponId = createNumber("couponId", Long.class);

    public final StringPath couponName = createString("couponName");

    public final SimplePath<com.example.order_service.common.domain.vo.Money> discountAmount = createSimple("discountAmount", com.example.order_service.common.domain.vo.Money.class);

    public QCartCouponSnapshot(String variable) {
        super(CartCouponSnapshot.class, forVariable(variable));
    }

    public QCartCouponSnapshot(Path<? extends CartCouponSnapshot> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCartCouponSnapshot(PathMetadata metadata) {
        super(CartCouponSnapshot.class, metadata);
    }

}

