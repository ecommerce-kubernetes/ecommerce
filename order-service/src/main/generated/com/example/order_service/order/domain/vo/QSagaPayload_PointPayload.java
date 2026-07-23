package com.example.order_service.order.domain.vo;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSagaPayload_PointPayload is a Querydsl query type for PointPayload
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QSagaPayload_PointPayload extends BeanPath<SagaPayload.PointPayload> {

    private static final long serialVersionUID = 858906444L;

    public static final QSagaPayload_PointPayload pointPayload = new QSagaPayload_PointPayload("pointPayload");

    public final SimplePath<com.example.order_service.common.domain.vo.Money> usedPoints = createSimple("usedPoints", com.example.order_service.common.domain.vo.Money.class);

    public QSagaPayload_PointPayload(String variable) {
        super(SagaPayload.PointPayload.class, forVariable(variable));
    }

    public QSagaPayload_PointPayload(Path<? extends SagaPayload.PointPayload> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSagaPayload_PointPayload(PathMetadata metadata) {
        super(SagaPayload.PointPayload.class, metadata);
    }

}

