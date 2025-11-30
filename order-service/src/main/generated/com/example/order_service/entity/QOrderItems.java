package com.example.order_service.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrderItems is a Querydsl query type for OrderItems
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOrderItems extends EntityPathBase<OrderItems> {

    private static final long serialVersionUID = 648364930L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOrderItems orderItems = new QOrderItems("orderItems");

    public final com.example.order_service.entity.base.QBaseEntity _super = new com.example.order_service.entity.base.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createAt = _super.createAt;

    public final NumberPath<Long> discountedPrice = createNumber("discountedPrice", Long.class);

    public final NumberPath<Integer> discountRate = createNumber("discountRate", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<com.example.order_service.dto.response.ItemOptionResponse, SimplePath<com.example.order_service.dto.response.ItemOptionResponse>> itemOption = this.<com.example.order_service.dto.response.ItemOptionResponse, SimplePath<com.example.order_service.dto.response.ItemOptionResponse>>createList("itemOption", com.example.order_service.dto.response.ItemOptionResponse.class, SimplePath.class, PathInits.DIRECT2);

    public final NumberPath<Long> lineTotal = createNumber("lineTotal", Long.class);

    public final QOrders order;

    public final NumberPath<Long> productId = createNumber("productId", Long.class);

    public final StringPath productName = createString("productName");

    public final NumberPath<Long> productVariantId = createNumber("productVariantId", Long.class);

    public final NumberPath<Integer> quantity = createNumber("quantity", Integer.class);

    public final StringPath thumbnail = createString("thumbnail");

    public final NumberPath<Long> unitPrice = createNumber("unitPrice", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateAt = _super.updateAt;

    public QOrderItems(String variable) {
        this(OrderItems.class, forVariable(variable), INITS);
    }

    public QOrderItems(Path<? extends OrderItems> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOrderItems(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOrderItems(PathMetadata metadata, PathInits inits) {
        this(OrderItems.class, metadata, inits);
    }

    public QOrderItems(Class<? extends OrderItems> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.order = inits.isInitialized("order") ? new QOrders(forProperty("order")) : null;
    }

}

