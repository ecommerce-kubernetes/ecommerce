package com.example.order_service.api.order.domain.model.vo;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QOrderItemPrice is a Querydsl query type for OrderItemPrice
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QOrderItemPrice extends BeanPath<OrderItemPrice> {

    private static final long serialVersionUID = -519950211L;

    public static final QOrderItemPrice orderItemPrice = new QOrderItemPrice("orderItemPrice");

    public final NumberPath<Long> discountAmount = createNumber("discountAmount", Long.class);

    public final NumberPath<Long> discountedPrice = createNumber("discountedPrice", Long.class);

    public final NumberPath<Integer> discountRate = createNumber("discountRate", Integer.class);

    public final NumberPath<Long> originPrice = createNumber("originPrice", Long.class);

    public QOrderItemPrice(String variable) {
        super(OrderItemPrice.class, forVariable(variable));
    }

    public QOrderItemPrice(Path<? extends OrderItemPrice> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOrderItemPrice(PathMetadata metadata) {
        super(OrderItemPrice.class, metadata);
    }

}

