package com.example.order_service.payment.application.mapper;

import com.example.order_service.payment.application.external.dto.result.PgPaymentResult;
import com.example.order_service.payment.application.service.dto.command.PaymentCommand;
import com.example.order_service.payment.application.service.dto.command.PaymentContext;
import com.example.order_service.payment.domain.model.PaymentStatus;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface PaymentMapper {

    @Mapping(source = "amount", target = "totalAmount")
    PaymentContext.Create toContext(PaymentCommand.Confirm command);
    @Mapping(source = "result.totalAmount", target = "amount")
    PaymentContext.Approval toContext(Long paymentId, PgPaymentResult.Approval result);
    @Mapping(source = "result.cancelAmount", target = "amount")
    PaymentContext.Cancellation toContext(Long paymentId, PaymentStatus status, PgPaymentResult.CancelReceipt result);
}
