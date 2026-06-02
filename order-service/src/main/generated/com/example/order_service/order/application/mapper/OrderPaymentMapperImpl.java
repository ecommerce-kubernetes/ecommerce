package com.example.order_service.order.application.mapper;

import com.example.order_service.infrastructure.dto.response.TossClientResponse;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-28T04:04:52+0900",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class OrderPaymentMapperImpl implements OrderPaymentMapper {

    @Override
    public OrderPaymentResult.Payment toPaymentResult(TossClientResponse.Confirm confirm) {
        if ( confirm == null ) {
            return null;
        }

        OrderPaymentResult.Payment.PaymentBuilder payment = OrderPaymentResult.Payment.builder();

        payment.orderNo( confirm.orderId() );
        payment.status( translateStatus( confirm.status() ) );
        payment.method( translateMethod( confirm.method() ) );
        payment.paymentKey( confirm.paymentKey() );
        payment.totalAmount( confirm.totalAmount() );
        payment.approvedAt( mapOffsetToLocal( confirm.approvedAt() ) );

        return payment.build();
    }
}
