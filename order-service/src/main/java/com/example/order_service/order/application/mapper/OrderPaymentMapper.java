package com.example.order_service.order.application.mapper;

import com.example.order_service.infrastructure.dto.response.TossClientResponse;
import com.example.order_service.order.application.dto.result.OrderPaymentResult;
import com.example.order_service.order.application.dto.result.PaymentMethod;
import com.example.order_service.order.application.dto.result.PaymentStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Mapper(componentModel = "spring")
public interface OrderPaymentMapper {

    @Mapping(source = "orderId", target = "orderNo")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "method", target = "method")
    OrderPaymentResult.Payment toPaymentResult(TossClientResponse.Confirm confirm);

    default PaymentStatus translateStatus(String status) {
        if (status == null) {
            return PaymentStatus.UNKNOWN;
        }
        return switch (status) {
            case "DONE" -> PaymentStatus.DONE;
            case "CANCELED" -> PaymentStatus.CANCELED;
            default -> PaymentStatus.UNKNOWN;
        };
    }

    default PaymentMethod translateMethod(String method) {
        if (method == null) {
            return PaymentMethod.UNKNOWN;
        }
        return switch (method) {
            case "카드" -> PaymentMethod.CARD;
            case "간편결제" -> PaymentMethod.EASY_PAYMENT;
            default -> PaymentMethod.UNKNOWN;
        };
    }

    default LocalDateTime mapOffsetToLocal(OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) {
            return null;
        }

        return offsetDateTime
                .atZoneSameInstant(ZoneId.of("Asia/Seoul"))
                .toLocalDateTime();
    }
}
