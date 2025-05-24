package com.example.product_service.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProductVariants is a Querydsl query type for ProductVariants
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProductVariants extends EntityPathBase<ProductVariants> {

    private static final long serialVersionUID = 259497004L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProductVariants productVariants = new QProductVariants("productVariants");

    public final NumberPath<Integer> discountValue = createNumber("discountValue", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> price = createNumber("price", Integer.class);

    public final QProducts product;

    public final StringPath sku = createString("sku");

    public final NumberPath<Integer> stockQuantity = createNumber("stockQuantity", Integer.class);

    public QProductVariants(String variable) {
        this(ProductVariants.class, forVariable(variable), INITS);
    }

    public QProductVariants(Path<? extends ProductVariants> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProductVariants(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProductVariants(PathMetadata metadata, PathInits inits) {
        this(ProductVariants.class, metadata, inits);
    }

    public QProductVariants(Class<? extends ProductVariants> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.product = inits.isInitialized("product") ? new QProducts(forProperty("product"), inits.get("product")) : null;
    }

}

