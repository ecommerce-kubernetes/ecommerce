package com.example.product_service.product.exception;

import com.example.product_service.common.exception.ErrorCategory;
import com.example.product_service.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCode {
    // 기본 에러
    CATEGORY_NOT_LEAF(ErrorCategory.INVALID_REQUEST, "CATEGORY_NOT_LEAF", "최하위 카테고리만 설정 가능합니다"),

    OPTION_TYPE_MISSING(ErrorCategory.SYSTEM_ERROR, "OPTION_TYPE_MISSING", "옵션 타입이 누락되었습니다"),
    CANNOT_REGISTER_OPTION_TYPE(ErrorCategory.BUSINESS_CONFLICT, "CANNOT_REGISTER_OPTION_TYPE", "판매중인 상품은 옵션 타입을 설정할 수 없습니다."),
    OPTION_TYPE_DUPLICATED(ErrorCategory.BUSINESS_CONFLICT, "OPTION_TYPE_DUPLICATED", "옵션 타입을 중복 설정할 수 없습니다"),
    CANNOT_DELETE_PRODUCT(ErrorCategory.BUSINESS_CONFLICT, "CANNOT_DELETE_PRODUCT", "판매중인 상품은 삭제할 수 없습니다"),
    PRODUCT_NOT_FOUND(ErrorCategory.NOT_FOUND, "PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다"),
    CANNOT_ADD_MAIN_IMAGE(ErrorCategory.BUSINESS_CONFLICT, "CANNOT_ADD_MAIN_IMAGE", "메인 이미지를 추가할 수 없는 상태 입니다"),
    CANNOT_ADD_DETAIL_IMAGE(ErrorCategory.BUSINESS_CONFLICT, "CANNOT_ADD_DETAIL_IMAGE", "설명 이미지를 추가할 수 없는 상태입니다"),
    EXCEED_MAX_OPTION_SIZE(ErrorCategory.BUSINESS_CONFLICT, "EXCEED_MAX_OPTION_SIZE", "적용할 수 있는 옵션 타입을 초과했습니다"),
    OPTION_TYPE_NOT_FOUND(ErrorCategory.BUSINESS_CONFLICT, "OPTION_TYPE_NOT_FOUND", "옵션 타입을 찾을 수 없습니다"),

    VARIANT_DUPLICATE_OPTION(ErrorCategory.BUSINESS_CONFLICT, "PRODUCT_025", "중복된 옵션 값의 상품 변형은 생성할 수 없습니다"),
    VARIANT_OUT_OF_STOCK(ErrorCategory.BUSINESS_CONFLICT, "PRODUCT_026", "상품 변형 재고가 부족합니다");

    private final ErrorCategory category;
    private final String code;
    private final String message;
}
