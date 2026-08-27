package com.example.order_service.order.exception;

import com.example.order_service.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderProductPortErrorCode implements ErrorCode {
    PRODUCT_CLIENT_ERROR(HttpStatus.CONFLICT, "PRODUCT_CLIENT_ERROR", "상품 정보를 확인할 수 없습니다."),
    PRODUCT_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "PRODUCT_SERVER_ERROR", "상품 시스템에 일시적인 오류가 발생했습니다."),
    PRODUCT_UNAVAILABLE_SERVER_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "PRODUCT_UNAVAILABLE_SERVER_ERROR", "상품 시스템이 현재 응답할 수 없습니다. 잠시 후 다시 시도해주세요."),
    PRODUCT_CIRCUIT_OPEN(HttpStatus.SERVICE_UNAVAILABLE, "PRODUCT_CIRCUIT_OPEN", "상품 연동이 일시적으로 지연되고 있습니다. 잠시 후 다시 시도해주세요.");
    private final HttpStatus status;
    private final String code;
    private final String message;
}
