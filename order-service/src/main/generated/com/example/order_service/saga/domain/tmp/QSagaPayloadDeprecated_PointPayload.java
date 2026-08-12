package com.example.order_service.saga.domain.tmp;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSagaPayloadDeprecated_PointPayload is a Querydsl query type for PointPayload
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QSagaPayloadDeprecated_PointPayload extends BeanPath<SagaPayloadDeprecated.PointPayload> {

    private static final long serialVersionUID = 1667587305L;

    public static final QSagaPayloadDeprecated_PointPayload pointPayload = new QSagaPayloadDeprecated_PointPayload("pointPayload");

    public final SimplePath<com.example.order_service.common.domain.vo.Money> usedPoints = createSimple("usedPoints", com.example.order_service.common.domain.vo.Money.class);

    public QSagaPayloadDeprecated_PointPayload(String variable) {
        super(SagaPayloadDeprecated.PointPayload.class, forVariable(variable));
    }

    public QSagaPayloadDeprecated_PointPayload(Path<? extends SagaPayloadDeprecated.PointPayload> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSagaPayloadDeprecated_PointPayload(PathMetadata metadata) {
        super(SagaPayloadDeprecated.PointPayload.class, metadata);
    }

}

