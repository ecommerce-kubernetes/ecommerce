package com.example.order_service.payment.application.external.mapper;

import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.response.TossClientResponse;
import com.example.order_service.payment.application.external.dto.result.PgPaymentResult;
import com.example.order_service.payment.domain.model.PaymentMethod;
import com.example.order_service.payment.domain.model.PaymentStatus;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-03T05:20:34+0900",
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

        approval.orderNo( response.orderId() );
        approval.paymentKey( response.paymentKey() );
        approval.totalAmount( moneyMapper.toMoney( response.totalAmount() ) );
        if ( response.status() != null ) {
            approval.status( Enum.valueOf( PaymentStatus.class, response.status() ) );
        }
        if ( response.method() != null ) {
            approval.method( Enum.valueOf( PaymentMethod.class, response.method() ) );
        }
        approval.approvedAt( map( response.approvedAt() ) );

        return approval.build();
    }
}
