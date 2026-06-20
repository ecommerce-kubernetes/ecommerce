package com.example.order_service.payment.application.external.mapper;

import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.response.TossClientResponse;
import com.example.order_service.payment.application.external.dto.result.PgPaymentResult;
import com.example.order_service.payment.domain.model.PaymentStatus;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-21T02:18:58+0900",
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
    public PgPaymentResult.Approval toResult(TossClientResponse.Confirm response) {
        if ( response == null ) {
            return null;
        }

        PgPaymentResult.Approval.ApprovalBuilder approval = PgPaymentResult.Approval.builder();

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
    public PgPaymentResult.Cancellation toResult(TossClientResponse.Cancel response) {
        if ( response == null ) {
            return null;
        }

        PgPaymentResult.Cancellation.CancellationBuilder cancellation = PgPaymentResult.Cancellation.builder();

        if ( response.status() != null ) {
            cancellation.status( Enum.valueOf( PaymentStatus.class, response.status() ) );
        }
        cancellation.cancels( cancelReceiptListToCancelReceiptList( response.cancels() ) );

        return cancellation.build();
    }

    protected PgPaymentResult.CancelReceipt cancelReceiptToCancelReceipt(TossClientResponse.CancelReceipt cancelReceipt) {
        if ( cancelReceipt == null ) {
            return null;
        }

        PgPaymentResult.CancelReceipt.CancelReceiptBuilder cancelReceipt1 = PgPaymentResult.CancelReceipt.builder();

        cancelReceipt1.transactionKey( cancelReceipt.transactionKey() );
        cancelReceipt1.cancelAmount( moneyMapper.toMoney( cancelReceipt.cancelAmount() ) );
        cancelReceipt1.cancelReason( cancelReceipt.cancelReason() );
        cancelReceipt1.canceledAt( map( cancelReceipt.canceledAt() ) );

        return cancelReceipt1.build();
    }

    protected List<PgPaymentResult.CancelReceipt> cancelReceiptListToCancelReceiptList(List<TossClientResponse.CancelReceipt> list) {
        if ( list == null ) {
            return null;
        }

        List<PgPaymentResult.CancelReceipt> list1 = new ArrayList<PgPaymentResult.CancelReceipt>( list.size() );
        for ( TossClientResponse.CancelReceipt cancelReceipt : list ) {
            list1.add( cancelReceiptToCancelReceipt( cancelReceipt ) );
        }

        return list1;
    }
}
