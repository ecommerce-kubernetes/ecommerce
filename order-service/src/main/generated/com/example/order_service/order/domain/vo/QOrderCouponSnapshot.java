package com.example.order_service.order.domain.vo;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QOrderCouponSnapshot is a Querydsl query type for OrderCouponSnapshot
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QOrderCouponSnapshot extends BeanPath<OrderCouponSnapshot> {

    private static final long serialVersionUID = -634788334L;

    public static final QOrderCouponSnapshot orderCouponSnapshot = new QOrderCouponSnapshot("orderCouponSnapshot");

    public final NumberPath<Long> couponId = createNumber("couponId", Long.class);

    public final StringPath couponName = createString("couponName");

    public final SimplePath<com.example.order_service.common.domain.vo.Money> discountAmount = createSimple("discountAmount", com.example.order_service.common.domain.vo.Money.class);

    public QOrderCouponSnapshot(String variable) {
        super(OrderCouponSnapshot.class, forVariable(variable));
    }

    public QOrderCouponSnapshot(Path<? extends OrderCouponSnapshot> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOrderCouponSnapshot(PathMetadata metadata) {
        super(OrderCouponSnapshot.class, metadata);
    }

}

