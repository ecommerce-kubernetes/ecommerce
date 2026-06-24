package com.example.order_service.order.domain.model;

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

    private static final long serialVersionUID = 1460828570L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOrder order = new QOrder("order1");

    public final com.example.order_service.common.entity.QBaseEntity _super = new com.example.order_service.common.entity.QBaseEntity(this);

    public final com.example.order_service.order.domain.vo.QOrderCouponSnapshot cartCoupon;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath failureReason = createString("failureReason");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.example.order_service.order.domain.vo.QOrderer orderer;

    public final ListPath<OrderItem, QOrderItem> orderItems = this.<OrderItem, QOrderItem>createList("orderItems", OrderItem.class, QOrderItem.class, PathInits.DIRECT2);

    public final StringPath orderName = createString("orderName");

    public final StringPath orderNo = createString("orderNo");

    public final com.example.order_service.order.domain.vo.QShippingAddress shippingAddress;

    public final EnumPath<OrderStatus> status = createEnum("status", OrderStatus.class);

    public final SimplePath<com.example.order_service.common.domain.vo.Money> totalCouponDiscountAmount = createSimple("totalCouponDiscountAmount", com.example.order_service.common.domain.vo.Money.class);

    public final SimplePath<com.example.order_service.common.domain.vo.Money> totalOriginalPrice = createSimple("totalOriginalPrice", com.example.order_service.common.domain.vo.Money.class);

    public final SimplePath<com.example.order_service.common.domain.vo.Money> totalPaymentAmount = createSimple("totalPaymentAmount", com.example.order_service.common.domain.vo.Money.class);

    public final SimplePath<com.example.order_service.common.domain.vo.Money> totalProductDiscountAmount = createSimple("totalProductDiscountAmount", com.example.order_service.common.domain.vo.Money.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final SimplePath<com.example.order_service.common.domain.vo.Money> usedPoints = createSimple("usedPoints", com.example.order_service.common.domain.vo.Money.class);

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
        this.cartCoupon = inits.isInitialized("cartCoupon") ? new com.example.order_service.order.domain.vo.QOrderCouponSnapshot(forProperty("cartCoupon")) : null;
        this.orderer = inits.isInitialized("orderer") ? new com.example.order_service.order.domain.vo.QOrderer(forProperty("orderer")) : null;
        this.shippingAddress = inits.isInitialized("shippingAddress") ? new com.example.order_service.order.domain.vo.QShippingAddress(forProperty("shippingAddress")) : null;
    }

}

