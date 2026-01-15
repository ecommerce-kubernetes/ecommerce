package com.example.product_service.api.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCode {
    CATEGORY_NOT_LEAF(409, "PRODUCT_001", "최하위 카테고리만 설정 가능합니다"),
    CANNOT_MODIFY_ON_SALE(409, "PRODUCT_002", "판매중인 상품은 옵션을 설정할 수 없습니다"),
    PRODUCT_NOT_FOUND(404, "PRODUCT_003", "상품을 찾을 수 없습니다"),
    HAS_VARIANTS(409, "PRODUCT_004", "상품에 속한 상품 변형이 존재합니다"),
    EXCEED_OPTION_SPEC_COUNT(409, "PRODUCT_005", "상품에 설정할 수 있는 옵션의 개수는 최대 3개 입니다"),
    DUPLICATE_OPTION_TYPE(409, "PRODUCT_006", "동일한 상품 옵션은 설정할 수 없습니다");
    private final int status;
    private final String code;
    private final String message;
}
