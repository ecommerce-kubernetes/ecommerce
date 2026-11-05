package com.example.order_service.order.domain.order;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QOrderAmount is a Querydsl query type for OrderAmount
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QOrderAmount extends BeanPath<OrderAmount> {

    private static final long serialVersionUID = 944641847L;

    public static final QOrderAmount orderAmount = new QOrderAmount("orderAmount");

    public final SimplePath<com.example.order_service.common.domain.vo.Money> cartCouponDiscount = createSimple("cartCouponDiscount", com.example.order_service.common.domain.vo.Money.class);

    public final SimplePath<com.example.order_service.common.domain.vo.Money> totalItemCouponDiscount = createSimple("totalItemCouponDiscount", com.example.order_service.common.domain.vo.Money.class);

    public final SimplePath<com.example.order_service.common.domain.vo.Money> totalItemDiscount = createSimple("totalItemDiscount", com.example.order_service.common.domain.vo.Money.class);

    public final SimplePath<com.example.order_service.common.domain.vo.Money> totalOriginalAmount = createSimple("totalOriginalAmount", com.example.order_service.common.domain.vo.Money.class);

    public final SimplePath<com.example.order_service.common.domain.vo.Money> totalPaymentAmount = createSimple("totalPaymentAmount", com.example.order_service.common.domain.vo.Money.class);

    public final SimplePath<com.example.order_service.common.domain.vo.Money> usedPoints = createSimple("usedPoints", com.example.order_service.common.domain.vo.Money.class);

    public QOrderAmount(String variable) {
        super(OrderAmount.class, forVariable(variable));
    }

    public QOrderAmount(Path<? extends OrderAmount> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOrderAmount(PathMetadata metadata) {
        super(OrderAmount.class, metadata);
    }

}

