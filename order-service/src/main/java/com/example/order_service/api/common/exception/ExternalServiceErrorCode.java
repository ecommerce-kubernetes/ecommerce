package com.example.order_service.api.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExternalServiceErrorCode implements ErrorCode {

    UNAVAILABLE(503, "EXT_SERVICE_001", "현재 서비스 이용이 원활하지 않습니다 다시 시도해주세요"),
    SYSTEM_ERROR(500, "EXT_SERVICE_002", "외부 시스템 연동 중 오류가 발생했습니다"),
    PRODUCT_NOT_FOUND(404, "EXT_SERVICE_003", "요청한 상품을 찾을 수 없습니다"),
    USER_NOT_FOUND(404, "EXT_SERVICE_004", "유저를 찾을 수 없습니다"),
    COUPON_NOT_FOUND(404, "EXT_SERVICE_005", "쿠폰을 찾을 수 없습니다"),
    COUPON_INVALID(409, "EXT_SERVICE_006", "유효하지 않은 쿠폰입니다");

    private final int status;
    private final String code;
    private final String message;
}
