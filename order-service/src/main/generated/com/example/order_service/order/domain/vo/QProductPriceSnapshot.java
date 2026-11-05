package com.example.order_service.order.domain.vo;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QProductPriceSnapshot is a Querydsl query type for ProductPriceSnapshot
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QProductPriceSnapshot extends BeanPath<ProductPriceSnapshot> {

    private static final long serialVersionUID = 1706667428L;

    public static final QProductPriceSnapshot productPriceSnapshot = new QProductPriceSnapshot("productPriceSnapshot");

    public final SimplePath<com.example.order_service.common.domain.vo.Money> discountAmount = createSimple("discountAmount", com.example.order_service.common.domain.vo.Money.class);

    public final SimplePath<com.example.order_service.common.domain.vo.Money> discountedPrice = createSimple("discountedPrice", com.example.order_service.common.domain.vo.Money.class);

    public final NumberPath<Integer> discountRate = createNumber("discountRate", Integer.class);

    public final SimplePath<com.example.order_service.common.domain.vo.Money> originalPrice = createSimple("originalPrice", com.example.order_service.common.domain.vo.Money.class);

    public QProductPriceSnapshot(String variable) {
        super(ProductPriceSnapshot.class, forVariable(variable));
    }

    public QProductPriceSnapshot(Path<? extends ProductPriceSnapshot> path) {
        super(path.getType(), path.getMetadata());
    }

    public QProductPriceSnapshot(PathMetadata metadata) {
        super(ProductPriceSnapshot.class, metadata);
    }

}

