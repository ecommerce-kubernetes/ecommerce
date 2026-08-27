package com.example.order_service.payment.exception;

import com.example.order_service.common.exception.ErrorCategory;
import com.example.order_service.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentOrderPortErrorCode implements ErrorCode {
    ORDER_NOT_FOUND(ErrorCategory.NOT_FOUND, "ORDER_NOT_FOUND", "주문을 찾을 수 없습니다."),
    ORDER_CLIENT_ERROR(ErrorCategory.BUSINESS_CONFLICT, "ORDER_CLIENT_ERROR", "주문 조회중 클라이언트 오류가 발생했습니다."),
    ORDER_SERVER_ERROR(ErrorCategory.SYSTEM_ERROR, "ORDER_SERVER_ERROR", "주문 조회중 서버 오류가 발생했습니다.");
    private final ErrorCategory category;
    private final String code;
    private final String message;
}
