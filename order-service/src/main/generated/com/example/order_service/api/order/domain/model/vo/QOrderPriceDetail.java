package com.example.order_service.api.order.domain.model.vo;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QOrderPriceDetail is a Querydsl query type for OrderPriceDetail
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QOrderPriceDetail extends BeanPath<OrderPriceDetail> {

    private static final long serialVersionUID = 492784577L;

    public static final QOrderPriceDetail orderPriceDetail = new QOrderPriceDetail("orderPriceDetail");

    public final NumberPath<Long> couponDiscount = createNumber("couponDiscount", Long.class);

    public final NumberPath<Long> finalPaymentAmount = createNumber("finalPaymentAmount", Long.class);

    public final NumberPath<Long> pointDiscount = createNumber("pointDiscount", Long.class);

    public final NumberPath<Long> totalOriginPrice = createNumber("totalOriginPrice", Long.class);

    public final NumberPath<Long> totalProductDiscount = createNumber("totalProductDiscount", Long.class);

    public QOrderPriceDetail(String variable) {
        super(OrderPriceDetail.class, forVariable(variable));
    }

    public QOrderPriceDetail(Path<? extends OrderPriceDetail> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOrderPriceDetail(PathMetadata metadata) {
        super(OrderPriceDetail.class, metadata);
    }

}

