package com.example.order_service.payment.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPayment is a Querydsl query type for Payment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPayment extends EntityPathBase<Payment> {

    private static final long serialVersionUID = 1353827167L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPayment payment = new QPayment("payment");

    public final com.example.order_service.common.entity.QBaseEntity _super = new com.example.order_service.common.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final QPaymentFailure failure;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<PaymentMethod> method = createEnum("method", PaymentMethod.class);

    public final NumberPath<Long> orderId = createNumber("orderId", Long.class);

    public final StringPath paymentKey = createString("paymentKey");

    public final ListPath<PaymentTransaction, QPaymentTransaction> paymentTransactions = this.<PaymentTransaction, QPaymentTransaction>createList("paymentTransactions", PaymentTransaction.class, QPaymentTransaction.class, PathInits.DIRECT2);

    public final EnumPath<PaymentProvider> provider = createEnum("provider", PaymentProvider.class);

    public final EnumPath<PaymentStatus> status = createEnum("status", PaymentStatus.class);

    public final SimplePath<com.example.order_service.common.domain.vo.Money> totalAmount = createSimple("totalAmount", com.example.order_service.common.domain.vo.Money.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QPayment(String variable) {
        this(Payment.class, forVariable(variable), INITS);
    }

    public QPayment(Path<? extends Payment> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPayment(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPayment(PathMetadata metadata, PathInits inits) {
        this(Payment.class, metadata, inits);
    }

    public QPayment(Class<? extends Payment> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.failure = inits.isInitialized("failure") ? new QPaymentFailure(forProperty("failure")) : null;
    }

}

