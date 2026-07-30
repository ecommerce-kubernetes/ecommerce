package com.example.order_service.order.domain.order;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QOrderItemAmount is a Querydsl query type for OrderItemAmount
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QOrderItemAmount extends BeanPath<OrderItemAmount> {

    private static final long serialVersionUID = 1436949674L;

    public static final QOrderItemAmount orderItemAmount = new QOrderItemAmount("orderItemAmount");

    public final SimplePath<com.example.order_service.common.domain.vo.Money> finalAmount = createSimple("finalAmount", com.example.order_service.common.domain.vo.Money.class);

    public final SimplePath<com.example.order_service.common.domain.vo.Money> itemCouponDiscount = createSimple("itemCouponDiscount", com.example.order_service.common.domain.vo.Money.class);

    public final SimplePath<com.example.order_service.common.domain.vo.Money> itemDiscount = createSimple("itemDiscount", com.example.order_service.common.domain.vo.Money.class);

    public final SimplePath<com.example.order_service.common.domain.vo.Money> lineTotal = createSimple("lineTotal", com.example.order_service.common.domain.vo.Money.class);

    public final SimplePath<com.example.order_service.common.domain.vo.Money> originalAmount = createSimple("originalAmount", com.example.order_service.common.domain.vo.Money.class);

    public QOrderItemAmount(String variable) {
        super(OrderItemAmount.class, forVariable(variable));
    }

    public QOrderItemAmount(Path<? extends OrderItemAmount> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOrderItemAmount(PathMetadata metadata) {
        super(OrderItemAmount.class, metadata);
    }

}

