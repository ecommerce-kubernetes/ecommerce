package com.example.order_service.order.domain.order;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QOrderCancelInfo is a Querydsl query type for OrderCancelInfo
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QOrderCancelInfo extends BeanPath<OrderCancelInfo> {

    private static final long serialVersionUID = 1846835623L;

    public static final QOrderCancelInfo orderCancelInfo = new QOrderCancelInfo("orderCancelInfo");

    public final DateTimePath<java.time.LocalDateTime> canceledAt = createDateTime("canceledAt", java.time.LocalDateTime.class);

    public final StringPath reason = createString("reason");

    public QOrderCancelInfo(String variable) {
        super(OrderCancelInfo.class, forVariable(variable));
    }

    public QOrderCancelInfo(Path<? extends OrderCancelInfo> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOrderCancelInfo(PathMetadata metadata) {
        super(OrderCancelInfo.class, metadata);
    }

}

