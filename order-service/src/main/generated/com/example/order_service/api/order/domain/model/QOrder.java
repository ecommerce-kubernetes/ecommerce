package com.example.order_service.api.order.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrder is a Querydsl query type for Order
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOrder extends EntityPathBase<Order> {

    private static final long serialVersionUID = -262041234L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOrder order = new QOrder("order1");

    public final com.example.order_service.api.common.entity.QBaseEntity _super = new com.example.order_service.api.common.entity.QBaseEntity(this);

    public final QCoupon coupon;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath deliveryAddress = createString("deliveryAddress");

    public final EnumPath<OrderFailureCode> failureCode = createEnum("failureCode", OrderFailureCode.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.example.order_service.api.order.domain.model.vo.QOrderer orderer;

    public final ListPath<OrderItem, QOrderItem> orderItems = this.<OrderItem, QOrderItem>createList("orderItems", OrderItem.class, QOrderItem.class, PathInits.DIRECT2);

    public final StringPath orderName = createString("orderName");

    public final StringPath orderNo = createString("orderNo");

    public final com.example.order_service.api.order.domain.model.vo.QOrderPriceDetail orderPriceDetail;

    public final ListPath<Payment, QPayment> payments = this.<Payment, QPayment>createList("payments", Payment.class, QPayment.class, PathInits.DIRECT2);

    public final EnumPath<OrderStatus> status = createEnum("status", OrderStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QOrder(String variable) {
        this(Order.class, forVariable(variable), INITS);
    }

    public QOrder(Path<? extends Order> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOrder(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOrder(PathMetadata metadata, PathInits inits) {
        this(Order.class, metadata, inits);
    }

    public QOrder(Class<? extends Order> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.coupon = inits.isInitialized("coupon") ? new QCoupon(forProperty("coupon"), inits.get("coupon")) : null;
        this.orderer = inits.isInitialized("orderer") ? new com.example.order_service.api.order.domain.model.vo.QOrderer(forProperty("orderer")) : null;
        this.orderPriceDetail = inits.isInitialized("orderPriceDetail") ? new com.example.order_service.api.order.domain.model.vo.QOrderPriceDetail(forProperty("orderPriceDetail")) : null;
    }

}

