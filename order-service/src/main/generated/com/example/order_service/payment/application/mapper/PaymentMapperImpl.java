package com.example.order_service.payment.application.mapper;

import com.example.order_service.payment.application.external.dto.result.PGPaymentResult;
import com.example.order_service.payment.application.service.dto.command.PaymentCommand;
import com.example.order_service.payment.application.service.dto.command.PaymentContext;
import com.example.order_service.payment.domain.model.PaymentStatus;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-25T22:29:20+0900",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class PaymentMapperImpl implements PaymentMapper {

    @Override
    public PaymentContext.Create toContext(PaymentCommand.Confirm command) {
        if ( command == null ) {
            return null;
        }

        PaymentContext.Create.CreateBuilder create = PaymentContext.Create.builder();

        create.totalAmount( command.amount() );
        create.userId( command.userId() );
        create.orderNo( command.orderNo() );
        create.paymentKey( command.paymentKey() );

        return create.build();
    }

    @Override
    public PaymentContext.Approval toContext(Long paymentId, PGPaymentResult.Approval result) {
        if ( paymentId == null && result == null ) {
            return null;
        }

        PaymentContext.Approval.ApprovalBuilder approval = PaymentContext.Approval.builder();

        if ( result != null ) {
            approval.amount( result.totalAmount() );
            approval.status( result.status() );
            approval.method( result.method() );
            approval.transactionKey( result.transactionKey() );
            approval.approvedAt( result.approvedAt() );
        }
        approval.paymentId( paymentId );

        return approval.build();
    }

    @Override
    public PaymentContext.Cancellation toContext(Long paymentId, PaymentStatus status, PGPaymentResult.CancelReceipt result) {
        if ( paymentId == null && status == null && result == null ) {
            return null;
        }

        PaymentContext.Cancellation.CancellationBuilder cancellation = PaymentContext.Cancellation.builder();

        if ( result != null ) {
            cancellation.amount( result.cancelAmount() );
            cancellation.transactionKey( result.transactionKey() );
            cancellation.cancelReason( result.cancelReason() );
            cancellation.canceledAt( result.canceledAt() );
        }
        cancellation.paymentId( paymentId );
        cancellation.status( status );

        return cancellation.build();
    }
}
