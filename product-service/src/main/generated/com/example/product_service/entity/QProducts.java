package com.example.product_service.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProducts is a Querydsl query type for Products
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProducts extends EntityPathBase<Products> {

    private static final long serialVersionUID = -2080105803L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProducts products = new QProducts("products");

    public final com.example.product_service.entity.base.QBaseEntity _super = new com.example.product_service.entity.base.QBaseEntity(this);

    public final QCategories category;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createAt = _super.createAt;

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<ProductImages, QProductImages> images = this.<ProductImages, QProductImages>createList("images", ProductImages.class, QProductImages.class, PathInits.DIRECT2);

    public final StringPath name = createString("name");

    public final ListPath<ProductOptionTypes, QProductOptionTypes> productOptionTypes = this.<ProductOptionTypes, QProductOptionTypes>createList("productOptionTypes", ProductOptionTypes.class, QProductOptionTypes.class, PathInits.DIRECT2);

    public final ListPath<ProductVariants, QProductVariants> productVariants = this.<ProductVariants, QProductVariants>createList("productVariants", ProductVariants.class, QProductVariants.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateAt = _super.updateAt;

    public QProducts(String variable) {
        this(Products.class, forVariable(variable), INITS);
    }

    public QProducts(Path<? extends Products> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProducts(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProducts(PathMetadata metadata, PathInits inits) {
        this(Products.class, metadata, inits);
    }

    public QProducts(Class<? extends Products> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.category = inits.isInitialized("category") ? new QCategories(forProperty("category"), inits.get("category")) : null;
    }

}

