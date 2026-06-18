package com.example.order_service.payment.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPaymentFailure is a Querydsl query type for PaymentFailure
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPaymentFailure extends EntityPathBase<PaymentFailure> {

    private static final long serialVersionUID = 1409182032L;

    public static final QPaymentFailure paymentFailure = new QPaymentFailure("paymentFailure");

    public final StringPath errorCode = createString("errorCode");

    public final DateTimePath<java.time.LocalDateTime> failedAt = createDateTime("failedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> paymentId = createNumber("paymentId", Long.class);

    public final StringPath reason = createString("reason");

    public QPaymentFailure(String variable) {
        super(PaymentFailure.class, forVariable(variable));
    }

    public QPaymentFailure(Path<? extends PaymentFailure> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPaymentFailure(PathMetadata metadata) {
        super(PaymentFailure.class, metadata);
    }

}

