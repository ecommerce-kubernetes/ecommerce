package com.example.order_service.payment.application.external.dto.command;

import com.example.order_service.common.domain.vo.Money;
import lombok.Builder;

public class PGPaymentCommand {

    @Builder
    public record Confirm(
            String orderNo,
            String paymentKey,
            Money amount
    ) {
        public static Confirm of(String orderNo, String paymentKey, Money amount) {
            return Confirm.builder()
                    .orderNo(orderNo)
                    .paymentKey(paymentKey)
                    .amount(amount)
                    .build();
        }
    }

    @Builder
    public record Cancel(
            String paymentKey,
            String cancelReason,
            Money amount
    ) {
        public static Cancel ofFull(String paymentKey, String cancelReason) {
            return Cancel.builder()
                    .paymentKey(paymentKey)
                    .cancelReason(cancelReason)
                    .amount(null)
                    .build();
        }

        public static Cancel ofPartial(String paymentKey, String cancelReason, Money amount) {
            return Cancel.builder()
                    .paymentKey(paymentKey)
                    .cancelReason(cancelReason)
                    .amount(amount)
                    .build();
        }
    }
}
