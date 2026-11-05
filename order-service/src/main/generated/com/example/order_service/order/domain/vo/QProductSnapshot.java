package com.example.order_service.order.domain.vo;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QProductSnapshot is a Querydsl query type for ProductSnapshot
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QProductSnapshot extends BeanPath<ProductSnapshot> {

    private static final long serialVersionUID = -1699402771L;

    public static final QProductSnapshot productSnapshot = new QProductSnapshot("productSnapshot");

    public final NumberPath<Long> productId = createNumber("productId", Long.class);

    public final StringPath productName = createString("productName");

    public final NumberPath<Long> productVariantId = createNumber("productVariantId", Long.class);

    public final StringPath sku = createString("sku");

    public final StringPath thumbnail = createString("thumbnail");

    public QProductSnapshot(String variable) {
        super(ProductSnapshot.class, forVariable(variable));
    }

    public QProductSnapshot(Path<? extends ProductSnapshot> path) {
        super(path.getType(), path.getMetadata());
    }

    public QProductSnapshot(PathMetadata metadata) {
        super(ProductSnapshot.class, metadata);
    }

}

