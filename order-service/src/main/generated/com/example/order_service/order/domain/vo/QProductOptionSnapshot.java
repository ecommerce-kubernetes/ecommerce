package com.example.order_service.order.domain.vo;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QProductOptionSnapshot is a Querydsl query type for ProductOptionSnapshot
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QProductOptionSnapshot extends BeanPath<ProductOptionSnapshot> {

    private static final long serialVersionUID = 870305986L;

    public static final QProductOptionSnapshot productOptionSnapshot = new QProductOptionSnapshot("productOptionSnapshot");

    public final StringPath optionTypeName = createString("optionTypeName");

    public final StringPath optionValueName = createString("optionValueName");

    public QProductOptionSnapshot(String variable) {
        super(ProductOptionSnapshot.class, forVariable(variable));
    }

    public QProductOptionSnapshot(Path<? extends ProductOptionSnapshot> path) {
        super(path.getType(), path.getMetadata());
    }

    public QProductOptionSnapshot(PathMetadata metadata) {
        super(ProductOptionSnapshot.class, metadata);
    }

}

