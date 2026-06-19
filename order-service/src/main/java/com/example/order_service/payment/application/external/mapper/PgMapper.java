package com.example.order_service.payment.application.external.mapper;

import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.response.TossClientResponse;
import com.example.order_service.payment.application.external.dto.result.PgPaymentResult;
import com.example.order_service.payment.domain.model.PaymentMethod;
import com.example.order_service.payment.exception.PaymentErrorCode;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Mapper(componentModel = "spring", uses = {MoneyMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface PgMapper {

    PgPaymentResult.Approval toResult(TossClientResponse.Confirm response);

    PgPaymentResult.Cancellation toResult(TossClientResponse.Cancel response);

    default LocalDateTime map(OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) {
            return null;
        }
        return offsetDateTime.toLocalDateTime();
    }

    default PaymentMethod map(String method) {
        if (method == null) {
            return null;
        }

        return switch (method) {
            case "카드" -> PaymentMethod.CARD;
            case "간편결제" -> PaymentMethod.EASY_PAYMENT;
            default -> throw new BusinessException(PaymentErrorCode.UNSUPPORTED_PAYMENT_METHOD);
        };
    }
}
