package com.example.order_service.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrders is a Querydsl query type for Orders
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOrders extends EntityPathBase<Orders> {

    private static final long serialVersionUID = -929067691L;

    public static final QOrders orders = new QOrders("orders");

    public final com.example.order_service.entity.base.QBaseEntity _super = new com.example.order_service.entity.base.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createAt = _super.createAt;

    public final StringPath deliveryAddress = createString("deliveryAddress");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<OrderItems, QOrderItems> orderItems = this.<OrderItems, QOrderItems>createList("orderItems", OrderItems.class, QOrderItems.class, PathInits.DIRECT2);

    public final StringPath status = createString("status");

    public final NumberPath<Integer> totalPrice = createNumber("totalPrice", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateAt = _super.updateAt;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QOrders(String variable) {
        super(Orders.class, forVariable(variable));
    }

    public QOrders(Path<? extends Orders> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOrders(PathMetadata metadata) {
        super(Orders.class, metadata);
    }

}

