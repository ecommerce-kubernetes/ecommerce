package com.example.product_service.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReviews is a Querydsl query type for Reviews
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReviews extends EntityPathBase<Reviews> {

    private static final long serialVersionUID = -1982808918L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReviews reviews = new QReviews("reviews");

    public final com.example.product_service.entity.base.QBaseEntity _super = new com.example.product_service.entity.base.QBaseEntity(this);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createAt = _super.createAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<ReviewImages, QReviewImages> images = this.<ReviewImages, QReviewImages>createList("images", ReviewImages.class, QReviewImages.class, PathInits.DIRECT2);

    public final QProductVariants productVariant;

    public final NumberPath<Integer> rating = createNumber("rating", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateAt = _super.updateAt;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QReviews(String variable) {
        this(Reviews.class, forVariable(variable), INITS);
    }

    public QReviews(Path<? extends Reviews> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReviews(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReviews(PathMetadata metadata, PathInits inits) {
        this(Reviews.class, metadata, inits);
    }

    public QReviews(Class<? extends Reviews> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.productVariant = inits.isInitialized("productVariant") ? new QProductVariants(forProperty("productVariant"), inits.get("productVariant")) : null;
    }

}

