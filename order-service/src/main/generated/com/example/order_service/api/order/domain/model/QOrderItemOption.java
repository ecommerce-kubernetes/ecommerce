package com.example.order_service.api.order.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrderItemOption is a Querydsl query type for OrderItemOption
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOrderItemOption extends EntityPathBase<OrderItemOption> {

    private static final long serialVersionUID = 363204982L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOrderItemOption orderItemOption = new QOrderItemOption("orderItemOption");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath optionTypeName = createString("optionTypeName");

    public final StringPath optionValueName = createString("optionValueName");

    public final QOrderItem orderItem;

    public QOrderItemOption(String variable) {
        this(OrderItemOption.class, forVariable(variable), INITS);
    }

    public QOrderItemOption(Path<? extends OrderItemOption> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOrderItemOption(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOrderItemOption(PathMetadata metadata, PathInits inits) {
        this(OrderItemOption.class, metadata, inits);
    }

    public QOrderItemOption(Class<? extends OrderItemOption> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.orderItem = inits.isInitialized("orderItem") ? new QOrderItem(forProperty("orderItem"), inits.get("orderItem")) : null;
    }

}

