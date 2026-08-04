package com.example.order_service.payment.domain;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PaymentStatus {
    APPROVAL_PENDING("승인 대기"),
    DONE("완료"),
    CANCELED("결제 취소"),
    PARTIAL_CANCELED("부분 취소"),
    ABORTED("결제 승인 실패"),
    WAITING_FOR_DEPOSIT("입금 대기"),
    MANUAL_CHECK("수동 점검"),
    REFUND_PENDING("결제 취소 대기");
    private final String description;
}
