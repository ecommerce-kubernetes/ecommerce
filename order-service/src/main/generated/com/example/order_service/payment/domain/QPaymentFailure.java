package com.example.order_service.payment.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPaymentFailure is a Querydsl query type for PaymentFailure
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QPaymentFailure extends BeanPath<PaymentFailure> {

    private static final long serialVersionUID = -362722005L;

    public static final QPaymentFailure paymentFailure = new QPaymentFailure("paymentFailure");

    public final StringPath code = createString("code");

    public final StringPath message = createString("message");

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

