package com.example.product_service.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOptionValues is a Querydsl query type for OptionValues
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOptionValues extends EntityPathBase<OptionValues> {

    private static final long serialVersionUID = 1984519880L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOptionValues optionValues = new QOptionValues("optionValues");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QOptionTypes optionType;

    public final StringPath value = createString("value");

    public QOptionValues(String variable) {
        this(OptionValues.class, forVariable(variable), INITS);
    }

    public QOptionValues(Path<? extends OptionValues> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOptionValues(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOptionValues(PathMetadata metadata, PathInits inits) {
        this(OptionValues.class, metadata, inits);
    }

    public QOptionValues(Class<? extends OptionValues> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.optionType = inits.isInitialized("optionType") ? new QOptionTypes(forProperty("optionType")) : null;
    }

}

