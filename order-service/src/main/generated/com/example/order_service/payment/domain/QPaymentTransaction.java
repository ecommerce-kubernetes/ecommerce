package com.example.order_service.payment.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPaymentTransaction is a Querydsl query type for PaymentTransaction
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPaymentTransaction extends EntityPathBase<PaymentTransaction> {

    private static final long serialVersionUID = 362624511L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPaymentTransaction paymentTransaction = new QPaymentTransaction("paymentTransaction");

    public final com.example.order_service.common.entity.QBaseEntity _super = new com.example.order_service.common.entity.QBaseEntity(this);

    public final SimplePath<com.example.order_service.common.domain.vo.Money> amount = createSimple("amount", com.example.order_service.common.domain.vo.Money.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> occurredAt = createDateTime("occurredAt", java.time.LocalDateTime.class);

    public final QPayment payment;

    public final StringPath reason = createString("reason");

    public final StringPath transactionKey = createString("transactionKey");

    public final EnumPath<TransactionType> type = createEnum("type", TransactionType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QPaymentTransaction(String variable) {
        this(PaymentTransaction.class, forVariable(variable), INITS);
    }

    public QPaymentTransaction(Path<? extends PaymentTransaction> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPaymentTransaction(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPaymentTransaction(PathMetadata metadata, PathInits inits) {
        this(PaymentTransaction.class, metadata, inits);
    }

    public QPaymentTransaction(Class<? extends PaymentTransaction> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.payment = inits.isInitialized("payment") ? new QPayment(forProperty("payment"), inits.get("payment")) : null;
    }

}

