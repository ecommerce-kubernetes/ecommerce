package com.example.payment_service.event;

public enum EventStatus {
    PENDING,   // 대기 중
    SUCCESS,   // 성공
    FAILED,   // 실패
    PROCESSING, // 수행 중
    ROLLBACK // 롤백
}
