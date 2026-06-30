package com.example.order_service.payment.application.external.mapper;

import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.response.TossClientResponse;
import com.example.order_service.payment.application.external.dto.result.PGPaymentResult;
import com.example.order_service.payment.domain.model.PaymentStatus;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-01T00:06:01+0900",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class PgMapperImpl implements PgMapper {

    private final MoneyMapper moneyMapper;

    @Autowired
    public PgMapperImpl(MoneyMapper moneyMapper) {

        this.moneyMapper = moneyMapper;
    }

    @Override
    public PGPaymentResult.Approval toResult(TossClientResponse.Confirm response) {
        if ( response == null ) {
            return null;
        }

        PGPaymentResult.Approval.ApprovalBuilder approval = PGPaymentResult.Approval.builder();

        approval.transactionKey( response.lastTransactionKey() );
        if ( response.status() != null ) {
            approval.status( Enum.valueOf( PaymentStatus.class, response.status() ) );
        }
        approval.totalAmount( moneyMapper.toMoney( response.totalAmount() ) );
        approval.method( map( response.method() ) );
        approval.approvedAt( map( response.approvedAt() ) );

        return approval.build();
    }

    @Override
    public PGPaymentResult.Cancellation toResult(TossClientResponse.Cancel response) {
        if ( response == null ) {
            return null;
        }

        PGPaymentResult.Cancellation.CancellationBuilder cancellation = PGPaymentResult.Cancellation.builder();

        if ( response.status() != null ) {
            cancellation.status( Enum.valueOf( PaymentStatus.class, response.status() ) );
        }
        cancellation.cancels( cancelReceiptListToCancelReceiptList( response.cancels() ) );

        return cancellation.build();
    }

    @Override
    public PGPaymentResult.Inquiry toResult(TossClientResponse.Inquiry response) {
        if ( response == null ) {
            return null;
        }

        PGPaymentResult.Inquiry.InquiryBuilder inquiry = PGPaymentResult.Inquiry.builder();

        inquiry.orderNo( response.orderId() );
        inquiry.paymentKey( response.paymentKey() );
        if ( response.status() != null ) {
            inquiry.status( Enum.valueOf( PaymentStatus.class, response.status() ) );
        }
        inquiry.totalAmount( moneyMapper.toMoney( response.totalAmount() ) );
        inquiry.balanceAmount( moneyMapper.toMoney( response.balanceAmount() ) );
        inquiry.method( map( response.method() ) );
        inquiry.lastTransactionKey( response.lastTransactionKey() );
        inquiry.approvedAt( map( response.approvedAt() ) );
        inquiry.failure( failureToFailureReason( response.failure() ) );
        inquiry.cancels( cancelReceiptListToCancelReceiptList( response.cancels() ) );

        return inquiry.build();
    }

    protected PGPaymentResult.CancelReceipt cancelReceiptToCancelReceipt(TossClientResponse.CancelReceipt cancelReceipt) {
        if ( cancelReceipt == null ) {
            return null;
        }

        PGPaymentResult.CancelReceipt.CancelReceiptBuilder cancelReceipt1 = PGPaymentResult.CancelReceipt.builder();

        cancelReceipt1.transactionKey( cancelReceipt.transactionKey() );
        cancelReceipt1.cancelAmount( moneyMapper.toMoney( cancelReceipt.cancelAmount() ) );
        cancelReceipt1.cancelReason( cancelReceipt.cancelReason() );
        cancelReceipt1.canceledAt( map( cancelReceipt.canceledAt() ) );

        return cancelReceipt1.build();
    }

    protected List<PGPaymentResult.CancelReceipt> cancelReceiptListToCancelReceiptList(List<TossClientResponse.CancelReceipt> list) {
        if ( list == null ) {
            return null;
        }

        List<PGPaymentResult.CancelReceipt> list1 = new ArrayList<PGPaymentResult.CancelReceipt>( list.size() );
        for ( TossClientResponse.CancelReceipt cancelReceipt : list ) {
            list1.add( cancelReceiptToCancelReceipt( cancelReceipt ) );
        }

        return list1;
    }

    protected PGPaymentResult.FailureReason failureToFailureReason(TossClientResponse.Failure failure) {
        if ( failure == null ) {
            return null;
        }

        PGPaymentResult.FailureReason.FailureReasonBuilder failureReason = PGPaymentResult.FailureReason.builder();

        failureReason.code( failure.code() );
        failureReason.message( failure.message() );

        return failureReason.build();
    }
}
