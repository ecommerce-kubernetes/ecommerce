package com.example.order_service.payment.application.external.mapper;

import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.response.TossClientResponse;
import com.example.order_service.payment.application.external.dto.result.PGPaymentResult;
import com.example.order_service.payment.domain.model.PaymentMethod;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Mapper(componentModel = "spring", uses = {MoneyMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface PgMapper {

    @Mapping(source = "lastTransactionKey", target = "transactionKey")
    PGPaymentResult.Approval toResult(TossClientResponse.Confirm response);

    PGPaymentResult.Cancellation toResult(TossClientResponse.Cancel response);

    @Mapping(source = "orderId", target = "orderNo")
    PGPaymentResult.Inquiry toResult(TossClientResponse.Inquiry response);

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
            case "가상계좌" -> PaymentMethod.VIRTUAL_ACCOUNT;
            case "휴대폰" -> PaymentMethod.PHONE;
            case "계좌이체" -> PaymentMethod.ACCOUNT_TRANSFER;
            case "문화상품권" -> PaymentMethod.CULTURE_GIFT_CERTIFICATE;
            case "도서문화상품권" -> PaymentMethod.BOOK_CULTURE_GIFT_CERTIFICATE;
            case "게임문화상품권" -> PaymentMethod.GAME_CULTURE_GIFT_CERTIFICATE;
            default -> PaymentMethod.UNKNOWN;
        };
    }
}
