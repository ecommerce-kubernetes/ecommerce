package com.example.order_service.payment.domain.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PaymentStatus {
    READY("승인 대기"),
    DONE("완료"),
    CANCELED("결제 취소"),
    ABORTED("결제 승인 실패"),
    WAITING_FOR_DEPOSIT("입금 대기"),
    REFUND_PENDING("결제 취소 대기");
    private final String description;
}
