package com.example.product_service.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReviewImages is a Querydsl query type for ReviewImages
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReviewImages extends EntityPathBase<ReviewImages> {

    private static final long serialVersionUID = -941703999L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReviewImages reviewImages = new QReviewImages("reviewImages");

    public final com.example.product_service.entity.base.QBaseEntity _super = new com.example.product_service.entity.base.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createAt = _super.createAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath imageUrl = createString("imageUrl");

    public final QReviews review;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateAt = _super.updateAt;

    public QReviewImages(String variable) {
        this(ReviewImages.class, forVariable(variable), INITS);
    }

    public QReviewImages(Path<? extends ReviewImages> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReviewImages(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReviewImages(PathMetadata metadata, PathInits inits) {
        this(ReviewImages.class, metadata, inits);
    }

    public QReviewImages(Class<? extends ReviewImages> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.review = inits.isInitialized("review") ? new QReviews(forProperty("review"), inits.get("review")) : null;
    }

}

