package com.example.order_service.order.domain.vo;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QOrderPriceSummary is a Querydsl query type for OrderPriceSummary
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QOrderPriceSummary extends BeanPath<OrderPriceSummary> {

    private static final long serialVersionUID = 870086181L;

    public static final QOrderPriceSummary orderPriceSummary = new QOrderPriceSummary("orderPriceSummary");

    public final SimplePath<com.example.order_service.common.domain.vo.Money> totalCouponDiscountAmount = createSimple("totalCouponDiscountAmount", com.example.order_service.common.domain.vo.Money.class);

    public final SimplePath<com.example.order_service.common.domain.vo.Money> totalOriginalPrice = createSimple("totalOriginalPrice", com.example.order_service.common.domain.vo.Money.class);

    public final SimplePath<com.example.order_service.common.domain.vo.Money> totalPaymentAmount = createSimple("totalPaymentAmount", com.example.order_service.common.domain.vo.Money.class);

    public final SimplePath<com.example.order_service.common.domain.vo.Money> totalProductDiscountAmount = createSimple("totalProductDiscountAmount", com.example.order_service.common.domain.vo.Money.class);

    public final SimplePath<com.example.order_service.common.domain.vo.Money> usedPoints = createSimple("usedPoints", com.example.order_service.common.domain.vo.Money.class);

    public QOrderPriceSummary(String variable) {
        super(OrderPriceSummary.class, forVariable(variable));
    }

    public QOrderPriceSummary(Path<? extends OrderPriceSummary> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOrderPriceSummary(PathMetadata metadata) {
        super(OrderPriceSummary.class, metadata);
    }

}

