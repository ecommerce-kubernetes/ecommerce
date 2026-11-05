package com.example.order_service.payment.domain;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PaymentStatus {
    READY("준비"),
    APPROVAL_PENDING("승인 대기"),
    DONE("완료"),
    CANCELED("결제 취소"),
    PARTIAL_CANCELED("부분 취소"),
    ABORTED("결제 실패"),
    MANUAL_CHECK("수동 점검"),
    REFUND_PENDING("결제 취소 대기");
    private final String description;
}
