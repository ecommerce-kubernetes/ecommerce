package com.example.order_service.api.order.domain.model.vo;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QOrderer is a Querydsl query type for Orderer
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QOrderer extends BeanPath<Orderer> {

    private static final long serialVersionUID = 1500599270L;

    public static final QOrderer orderer = new QOrderer("orderer");

    public final StringPath phoneNumber = createString("phoneNumber");

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final StringPath userName = createString("userName");

    public QOrderer(String variable) {
        super(Orderer.class, forVariable(variable));
    }

    public QOrderer(Path<? extends Orderer> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOrderer(PathMetadata metadata) {
        super(Orderer.class, metadata);
    }

}

