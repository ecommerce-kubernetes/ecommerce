package com.example.order_service.payment.application.mapper;

import com.example.order_service.payment.application.external.dto.result.PgPaymentResult;
import com.example.order_service.payment.application.service.dto.command.PaymentContext;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface PaymentMapper {

    @Mapping(source = "result.totalAmount", target = "amount")
    PaymentContext toContext(Long userId, PgPaymentResult.Approval result);
}
