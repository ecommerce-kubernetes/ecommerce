package com.example.order_service.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCarts is a Querydsl query type for Carts
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCarts extends EntityPathBase<Carts> {

    private static final long serialVersionUID = -872828701L;

    public static final QCarts carts = new QCarts("carts");

    public final com.example.order_service.entity.base.QBaseEntity _super = new com.example.order_service.entity.base.QBaseEntity(this);

    public final ListPath<CartItems, QCartItems> cartItems = this.<CartItems, QCartItems>createList("cartItems", CartItems.class, QCartItems.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createAt = _super.createAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateAt = _super.updateAt;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QCarts(String variable) {
        super(Carts.class, forVariable(variable));
    }

    public QCarts(Path<? extends Carts> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCarts(PathMetadata metadata) {
        super(Carts.class, metadata);
    }

}

