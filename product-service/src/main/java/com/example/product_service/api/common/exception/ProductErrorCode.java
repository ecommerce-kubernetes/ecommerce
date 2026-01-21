package com.example.product_service.api.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCode {
    // 기본 에러 
    PRODUCT_NOT_FOUND(404, "PRODUCT_001", "상품을 찾을 수 없습니다"),
    PRODUCT_CATEGORY_REQUIRED(400, "PRODUCT_002", "상품은 반드시 하나의 카테고리에 속해야 합니다"),
    CATEGORY_NOT_LEAF(400, "PRODUCT_003", "최하위 카테고리만 설정 가능합니다"),
    DELETED_PRODUCT_CANNOT_PUBLISH(409, "PRODUCT_004", "삭제된 상품은 게시할 수 없습니다"),
    INVALID_STATUS_FOR_STOP_SALE(409, "PRODUCT_005", "판매 중지할 수 없는 상품입니다"),
    PRODUCT_VARIANT_NOT_FOUND(404, "PRODUCT_006", "상품 변형을 찾을 수 없습니다"),

    // 상품 옵션 관련 에러
    OPTION_MODIFICATION_NOT_ALLOWED_ON_SALE(409, "PRODUCT_010", "판매중인 상품은 옵션을 수정할 수 없습니다"),
    OPTION_MODIFICATION_NOT_ALLOWED_WITH_VARIANT(409, "PRODUCT_011", "상품 변형이 존재하는 경우 옵션을 수정할 수 없습니다"),
    OPTION_COUNT_LIMIT_EXCEEDED(400, "PRODUCT_012", "상품 옵션은 최대 3개까지 설정 가능합니다"),
    OPTION_TYPE_DUPLICATED(400, "PRODUCT_013", "중복된 상품 옵션이 존재합니다"),

    VARIANT_OPTION_SIZE_MISMATCH(400, "PRODUCT_020", "상품 옵션 개수와 입력된 변형 옵션 값의 개수가 일치하지 않습니다"),
    VARIANT_OPTION_SPEC_MISMATCH(400, "PRODUCT_021", "상품에 설정된 옵션 스펙과 일치하지 않는 옵션 값이 존재합니다"),
    VARIANT_ALREADY_EXISTS(409, "PRODUCT_022", "이미 동일한 옵션 조합의 상품 변형이 존재합니다"),
    VARIANT_DUPLICATED_IN_REQUEST(400, "PRODUCT_023", "요청 내에 중복된 옵션 조합이 포함되어있습니다"),
    VARIANT_REQUIRED_FOR_PUBLISH(409, "PRODUCT_024", "상품 변형이 없는 상품은 게시할 수 없습니다"),
    VARIANT_DUPLICATE_OPTION(409, "PRODUCT_025", "중복된 옵션 값의 상품 변형은 생성할 수 없습니다"),

    THUMBNAIL_IMAGE_REQUIRED(409, "PRODUCT_030", "대표 이미지가 없는 상품은 게시할 수 없습니다"),
    IMAGE_REQUIRED_ON_SALE(409, "PRODUCT_031", "판매중인 상품은 최소 1개 이상의 이미지가 필요합니다"),

    DISPLAY_PRICE_INVALID(409, "PRODUCT_040", "판매가가 유효하지 않습니다"),
    ORIGINAL_PRICE_INVALID(409, "PRODUCT_041", "정가가 유효하지 않습니다"),
    DISCOUNT_RATE_INVALID(409, "PRODUCT_042", "할인율이 유효하지 않습니다"),
    DISPLAY_PRICE_EXCEEDS_ORIGINAL(409, "PRODUCT_043", "판매가는 정가보다 클 수 없습니다");
    private final int status;
    private final String code;
    private final String message;
}
