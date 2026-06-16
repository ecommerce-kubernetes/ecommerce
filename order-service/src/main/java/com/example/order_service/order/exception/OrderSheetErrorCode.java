package com.example.order_service.order.exception;

import com.example.order_service.common.exception.business.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderSheetErrorCode implements ErrorCode {
    ORDER_SHEET_PRODUCT_UNAVAILABLE_SERVER_ERROR(503, "ORDER-SHEET-007", "주문 처리중 일시적인 오류가 발생했습니다 잠시후 재시도 해주세요"),
    // 해당 코드(PRODUCT_CLIENT_ERROR)는 상품 서비스에서 발생한 400번대의 오류를 처리하기 위한 임시 에러 코드 (기능 확장 시 코드 추가하여 매핑)
    ORDER_SHEET_PRODUCT_CLIENT_ERROR(409, "ORDER-SHEET_006", "주문 처리중 클라이언트 오류가 발생했습니다"),
    ORDER_SHEET_PRODUCT_SERVER_ERROR(500, "ORDER-SHEET_005", "주문 처리중 오류가 발생했습니다");

    private final int status;
    private final String code;
    private final String message;

}
