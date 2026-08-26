package com.example.order_service.payment.adapter.out.client.pg.toss;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.infrastructure.dto.response.pg.TossCancelResponse;
import com.example.order_service.infrastructure.dto.response.pg.TossConfirmResponse;
import com.example.order_service.payment.application.port.dto.PGCancelResult;
import com.example.order_service.payment.application.port.dto.PGConfirmResult;
import com.example.order_service.payment.application.port.dto.PaymentPGStatus;
import com.example.order_service.payment.domain.PaymentMethod;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TossPGMapper {

    public PGConfirmResult toConfirmResult(TossConfirmResponse response) {
        PaymentPGStatus status = mapToPGStatus(response.status());
        PaymentMethod method = mapToMethod(response.method());
        LocalDateTime approvedAt = response.approvedAt().toLocalDateTime();
        return PGConfirmResult.builder()
                .status(status)
                .amount(Money.wons(response.totalAmount()))
                .method(method)
                .transactionKey(response.lastTransactionKey())
                .approvedAt(approvedAt)
                .build();
    }

    public PGCancelResult toCancelResult(TossCancelResponse response) {
        TossCancelResponse.CancelReceipt latestCancel = response.cancels().getLast();
        PaymentPGStatus status = mapToPGStatus(response.status());
        LocalDateTime canceledAt = latestCancel.canceledAt().toLocalDateTime();
        return PGCancelResult.builder()
                .status(status)
                .transactionKey(latestCancel.transactionKey())
                .amount(Money.wons(latestCancel.cancelAmount()))
                .cancelReason(latestCancel.cancelReason())
                .canceledAt(canceledAt)
                .build();
    }

    private PaymentPGStatus mapToPGStatus(String status) {
        return switch (status) {
            case "WAITING_FOR_DEPOSIT" -> PaymentPGStatus.WAITING_FOR_DEPOSIT;
            case "DONE" -> PaymentPGStatus.DONE;
            case "CANCELED" -> PaymentPGStatus.CANCELED;
            case "PARTIAL_CANCELED" -> PaymentPGStatus.PARTIAL_CANCELED;
            case "ABORT" -> PaymentPGStatus.ABORTED;
            case "EXPIRED" -> PaymentPGStatus.EXPIRED;
            default -> PaymentPGStatus.UNKNOWN;
        };
    }

    private PaymentMethod mapToMethod(String method) {
        return switch (method) {
            case "카드" -> PaymentMethod.CARD;
            case "가상계좌" -> PaymentMethod.VIRTUAL_ACCOUNT;
            case "간편결제" -> PaymentMethod.EASY_PAYMENT;
            case "휴대폰" -> PaymentMethod.PHONE;
            case "계좌이체" -> PaymentMethod.ACCOUNT_TRANSFER;
            case "문화상품권" -> PaymentMethod.CULTURE_GIFT_CERTIFICATE;
            case "도서문화상품권" -> PaymentMethod.BOOK_CULTURE_GIFT_CERTIFICATE;
            case "게임문화상품권" -> PaymentMethod.GAME_CULTURE_GIFT_CERTIFICATE;
            default -> PaymentMethod.UNKNOWN;
        };
    }

}
