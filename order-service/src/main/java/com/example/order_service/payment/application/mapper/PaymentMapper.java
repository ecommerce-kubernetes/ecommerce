package com.example.order_service.payment.application.mapper;

import com.example.order_service.payment.application.external.dto.result.PgPaymentResult;
import com.example.order_service.payment.application.service.dto.command.PaymentCommand;
import com.example.order_service.payment.application.service.dto.command.PaymentContext;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface PaymentMapper {

    @Mapping(source = "amount", target = "totalAmount")
    PaymentContext.Create toContext(PaymentCommand.Confirm command);
    @Mapping(source = "result.totalAmount", target = "amount")
    PaymentContext.Approval toContext(Long paymentId, PgPaymentResult.Approval result);
//    @Mapping(source = "result.totalAmount", target = "amount")
    PaymentContext.Cancellation toContext(Long paymentId, PgPaymentResult.Cancellation result);

    PaymentContext.Cancellation toContext(Long paymentId, PgPaymentResult.CancelReceipt result);
}
