package com.example.order_service.api.order.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrderItem is a Querydsl query type for OrderItem
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOrderItem extends EntityPathBase<OrderItem> {

    private static final long serialVersionUID = -647882335L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOrderItem orderItem = new QOrderItem("orderItem");

    public final com.example.order_service.api.common.entity.QBaseEntity _super = new com.example.order_service.api.common.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> lineTotal = createNumber("lineTotal", Long.class);

    public final QOrder order;

    public final com.example.order_service.api.order.domain.model.vo.QOrderedProduct orderedProduct;

    public final ListPath<OrderItemOption, QOrderItemOption> orderItemOptions = this.<OrderItemOption, QOrderItemOption>createList("orderItemOptions", OrderItemOption.class, QOrderItemOption.class, PathInits.DIRECT2);

    public final com.example.order_service.api.order.domain.model.vo.QOrderItemPrice orderItemPrice;

    public final NumberPath<Integer> quantity = createNumber("quantity", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QOrderItem(String variable) {
        this(OrderItem.class, forVariable(variable), INITS);
    }

    public QOrderItem(Path<? extends OrderItem> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOrderItem(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOrderItem(PathMetadata metadata, PathInits inits) {
        this(OrderItem.class, metadata, inits);
    }

    public QOrderItem(Class<? extends OrderItem> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.order = inits.isInitialized("order") ? new QOrder(forProperty("order"), inits.get("order")) : null;
        this.orderedProduct = inits.isInitialized("orderedProduct") ? new com.example.order_service.api.order.domain.model.vo.QOrderedProduct(forProperty("orderedProduct")) : null;
        this.orderItemPrice = inits.isInitialized("orderItemPrice") ? new com.example.order_service.api.order.domain.model.vo.QOrderItemPrice(forProperty("orderItemPrice")) : null;
    }

}

