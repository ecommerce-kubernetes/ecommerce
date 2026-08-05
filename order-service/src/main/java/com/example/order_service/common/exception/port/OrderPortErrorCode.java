package com.example.order_service.common.exception.port;

import com.example.order_service.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderPortErrorCode implements ErrorCode {
    ORDER_CLIENT_ERROR(HttpStatus.CONFLICT, "ORDER_CLIENT_ERROR", "주문 조회중 클라이언트 오류가 발생했습니다."),
    ORDER_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "ORDER_SERVER_ERROR", "주문 조회중 서버 오류가 발생했습니다.");
    private final HttpStatus status;
    private final String code;
    private final String message;
}
