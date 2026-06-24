package com.example.order_service.payment.domain.model;

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

    private static final long serialVersionUID = 832165594L;

    public static final QPayment payment = new QPayment("payment");

    public final com.example.order_service.common.entity.QBaseEntity _super = new com.example.order_service.common.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath failureCode = createString("failureCode");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath lastTransactionKey = createString("lastTransactionKey");

    public final EnumPath<PaymentMethod> method = createEnum("method", PaymentMethod.class);

    public final StringPath orderNo = createString("orderNo");

    public final StringPath paymentKey = createString("paymentKey");

    public final ListPath<PaymentRecord, QPaymentRecord> paymentRecords = this.<PaymentRecord, QPaymentRecord>createList("paymentRecords", PaymentRecord.class, QPaymentRecord.class, PathInits.DIRECT2);

    public final DateTimePath<java.time.LocalDateTime> refundPendingAt = createDateTime("refundPendingAt", java.time.LocalDateTime.class);

    public final EnumPath<PaymentStatus> status = createEnum("status", PaymentStatus.class);

    public final SimplePath<com.example.order_service.common.domain.vo.Money> totalAmount = createSimple("totalAmount", com.example.order_service.common.domain.vo.Money.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QPayment(String variable) {
        super(Payment.class, forVariable(variable));
    }

    public QPayment(Path<? extends Payment> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPayment(PathMetadata metadata) {
        super(Payment.class, metadata);
    }

}

