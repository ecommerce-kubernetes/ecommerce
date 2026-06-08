package com.example.order_service.payment.application.mapper;

import com.example.order_service.payment.application.external.dto.result.PgPaymentResult;
import com.example.order_service.payment.application.service.dto.command.PaymentCommand;
import com.example.order_service.payment.application.service.dto.command.PaymentContext;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-09T01:43:24+0900",
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
    public PaymentContext.Approval toContext(Long paymentId, PgPaymentResult.Approval result) {
        if ( paymentId == null && result == null ) {
            return null;
        }

        PaymentContext.Approval.ApprovalBuilder approval = PaymentContext.Approval.builder();

        if ( result != null ) {
            approval.amount( result.totalAmount() );
            approval.status( result.status() );
            approval.method( result.method() );
            approval.approvedAt( result.approvedAt() );
        }
        approval.paymentId( paymentId );

        return approval.build();
    }

    @Override
    public PaymentContext.Cancellation toContext(Long paymentId, PgPaymentResult.Cancellation result) {
        if ( paymentId == null && result == null ) {
            return null;
        }

        PaymentContext.Cancellation.CancellationBuilder cancellation = PaymentContext.Cancellation.builder();

        if ( result != null ) {
            cancellation.amount( result.totalAmount() );
            cancellation.status( result.status() );
            cancellation.method( result.method() );
            cancellation.approvedAt( result.approvedAt() );
        }
        cancellation.paymentId( paymentId );

        return cancellation.build();
    }
}
