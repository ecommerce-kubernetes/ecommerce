package com.example.order_service.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCartItems is a Querydsl query type for CartItems
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCartItems extends EntityPathBase<CartItems> {

    private static final long serialVersionUID = -502861808L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCartItems cartItems = new QCartItems("cartItems");

    public final QCarts cart;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> productId = createNumber("productId", Long.class);

    public final NumberPath<Integer> quantity = createNumber("quantity", Integer.class);

    public QCartItems(String variable) {
        this(CartItems.class, forVariable(variable), INITS);
    }

    public QCartItems(Path<? extends CartItems> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCartItems(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCartItems(PathMetadata metadata, PathInits inits) {
        this(CartItems.class, metadata, inits);
    }

    public QCartItems(Class<? extends CartItems> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.cart = inits.isInitialized("cart") ? new QCarts(forProperty("cart")) : null;
    }

}

