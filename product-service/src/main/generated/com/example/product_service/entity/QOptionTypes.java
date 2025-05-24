package com.example.product_service.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QOptionTypes is a Querydsl query type for OptionTypes
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOptionTypes extends EntityPathBase<OptionTypes> {

    private static final long serialVersionUID = 478530067L;

    public static final QOptionTypes optionTypes = new QOptionTypes("optionTypes");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public QOptionTypes(String variable) {
        super(OptionTypes.class, forVariable(variable));
    }

    public QOptionTypes(Path<? extends OptionTypes> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOptionTypes(PathMetadata metadata) {
        super(OptionTypes.class, metadata);
    }

}

