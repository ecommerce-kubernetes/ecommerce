package com.example.product_service.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProductOptionTypes is a Querydsl query type for ProductOptionTypes
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProductOptionTypes extends EntityPathBase<ProductOptionTypes> {

    private static final long serialVersionUID = 966080678L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProductOptionTypes productOptionTypes = new QProductOptionTypes("productOptionTypes");

    public final com.example.product_service.entity.base.QBaseEntity _super = new com.example.product_service.entity.base.QBaseEntity(this);

    public final BooleanPath active = createBoolean("active");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createAt = _super.createAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QOptionTypes optionType;

    public final NumberPath<Integer> priority = createNumber("priority", Integer.class);

    public final QProducts product;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateAt = _super.updateAt;

    public QProductOptionTypes(String variable) {
        this(ProductOptionTypes.class, forVariable(variable), INITS);
    }

    public QProductOptionTypes(Path<? extends ProductOptionTypes> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProductOptionTypes(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProductOptionTypes(PathMetadata metadata, PathInits inits) {
        this(ProductOptionTypes.class, metadata, inits);
    }

    public QProductOptionTypes(Class<? extends ProductOptionTypes> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.optionType = inits.isInitialized("optionType") ? new QOptionTypes(forProperty("optionType")) : null;
        this.product = inits.isInitialized("product") ? new QProducts(forProperty("product"), inits.get("product")) : null;
    }

}

