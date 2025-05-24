package com.example.product_service.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProductVariantOptions is a Querydsl query type for ProductVariantOptions
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProductVariantOptions extends EntityPathBase<ProductVariantOptions> {

    private static final long serialVersionUID = 322390903L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProductVariantOptions productVariantOptions = new QProductVariantOptions("productVariantOptions");

    public final com.example.product_service.entity.base.QBaseEntity _super = new com.example.product_service.entity.base.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createAt = _super.createAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QOptionValues optionValue;

    public final QProductVariants productVariant;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateAt = _super.updateAt;

    public QProductVariantOptions(String variable) {
        this(ProductVariantOptions.class, forVariable(variable), INITS);
    }

    public QProductVariantOptions(Path<? extends ProductVariantOptions> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProductVariantOptions(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProductVariantOptions(PathMetadata metadata, PathInits inits) {
        this(ProductVariantOptions.class, metadata, inits);
    }

    public QProductVariantOptions(Class<? extends ProductVariantOptions> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.optionValue = inits.isInitialized("optionValue") ? new QOptionValues(forProperty("optionValue"), inits.get("optionValue")) : null;
        this.productVariant = inits.isInitialized("productVariant") ? new QProductVariants(forProperty("productVariant"), inits.get("productVariant")) : null;
    }

}

