package com.example.order_service.payment.domain;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PaymentManualCheckReason {
    APPROVAL_RECON("결제 승인 확인"),
    REFUND_RECON("결제 환불 확인");
    private final String description;
}
