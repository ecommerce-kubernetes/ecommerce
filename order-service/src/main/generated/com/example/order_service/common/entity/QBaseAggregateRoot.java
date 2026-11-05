package com.example.order_service.common.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QBaseAggregateRoot is a Querydsl query type for BaseAggregateRoot
 */
@Generated("com.querydsl.codegen.DefaultSupertypeSerializer")
public class QBaseAggregateRoot extends EntityPathBase<BaseAggregateRoot> {

    private static final long serialVersionUID = 737578957L;

    public static final QBaseAggregateRoot baseAggregateRoot = new QBaseAggregateRoot("baseAggregateRoot");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QBaseAggregateRoot(String variable) {
        super(BaseAggregateRoot.class, forVariable(variable));
    }

    public QBaseAggregateRoot(Path<? extends BaseAggregateRoot> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBaseAggregateRoot(PathMetadata metadata) {
        super(BaseAggregateRoot.class, metadata);
    }

}

