package com.example.order_service.api.order.domain.model.vo;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QOrderedProduct is a Querydsl query type for OrderedProduct
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QOrderedProduct extends BeanPath<OrderedProduct> {

    private static final long serialVersionUID = 1304399351L;

    public static final QOrderedProduct orderedProduct = new QOrderedProduct("orderedProduct");

    public final NumberPath<Long> productId = createNumber("productId", Long.class);

    public final StringPath productName = createString("productName");

    public final NumberPath<Long> productVariantId = createNumber("productVariantId", Long.class);

    public final StringPath sku = createString("sku");

    public final StringPath thumbnail = createString("thumbnail");

    public QOrderedProduct(String variable) {
        super(OrderedProduct.class, forVariable(variable));
    }

    public QOrderedProduct(Path<? extends OrderedProduct> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOrderedProduct(PathMetadata metadata) {
        super(OrderedProduct.class, metadata);
    }

}

