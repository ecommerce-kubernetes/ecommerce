package com.example.order_service.order.exception;

import com.example.order_service.common.exception.business.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SagaErrorCode implements ErrorCode {
    SAGA_INSTANCE_NOT_FOUND(404, "SAGA_INSTANCE_NOT_FOUND", "주문 Saga 인스턴스를 찾을 수 없습니다");
    private final int status;
    private final String code;
    private final String message;
}
