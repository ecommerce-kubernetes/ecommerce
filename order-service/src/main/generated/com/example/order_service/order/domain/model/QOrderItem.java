package com.example.order_service.order.domain.model;

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

    private static final long serialVersionUID = 801835981L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOrderItem orderItem = new QOrderItem("orderItem");

    public final com.example.order_service.common.entity.QBaseEntity _super = new com.example.order_service.common.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.example.order_service.order.domain.vo.QOrderCouponSnapshot itemCoupon;

    public final ListPath<com.example.order_service.order.domain.vo.ProductOptionSnapshot, SimplePath<com.example.order_service.order.domain.vo.ProductOptionSnapshot>> options = this.<com.example.order_service.order.domain.vo.ProductOptionSnapshot, SimplePath<com.example.order_service.order.domain.vo.ProductOptionSnapshot>>createList("options", com.example.order_service.order.domain.vo.ProductOptionSnapshot.class, SimplePath.class, PathInits.DIRECT2);

    public final QOrder order;

    public final com.example.order_service.order.domain.vo.QProductSnapshot product;

    public final com.example.order_service.order.domain.vo.QProductPriceSnapshot productPrice;

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
        this.itemCoupon = inits.isInitialized("itemCoupon") ? new com.example.order_service.order.domain.vo.QOrderCouponSnapshot(forProperty("itemCoupon")) : null;
        this.order = inits.isInitialized("order") ? new QOrder(forProperty("order"), inits.get("order")) : null;
        this.product = inits.isInitialized("product") ? new com.example.order_service.order.domain.vo.QProductSnapshot(forProperty("product")) : null;
        this.productPrice = inits.isInitialized("productPrice") ? new com.example.order_service.order.domain.vo.QProductPriceSnapshot(forProperty("productPrice")) : null;
    }

}

