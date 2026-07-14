package com.example.order_service.order.domain.vo;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QItemCouponSnapshot is a Querydsl query type for ItemCouponSnapshot
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QItemCouponSnapshot extends BeanPath<ItemCouponSnapshot> {

    private static final long serialVersionUID = -1261878365L;

    public static final QItemCouponSnapshot itemCouponSnapshot = new QItemCouponSnapshot("itemCouponSnapshot");

    public final SimplePath<com.example.order_service.common.domain.vo.Money> discountAmount = createSimple("discountAmount", com.example.order_service.common.domain.vo.Money.class);

    public final NumberPath<Long> itemCouponId = createNumber("itemCouponId", Long.class);

    public final StringPath itemCouponName = createString("itemCouponName");

    public QItemCouponSnapshot(String variable) {
        super(ItemCouponSnapshot.class, forVariable(variable));
    }

    public QItemCouponSnapshot(Path<? extends ItemCouponSnapshot> path) {
        super(path.getType(), path.getMetadata());
    }

    public QItemCouponSnapshot(PathMetadata metadata) {
        super(ItemCouponSnapshot.class, metadata);
    }

}

