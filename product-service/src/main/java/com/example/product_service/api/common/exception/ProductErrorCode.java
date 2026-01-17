package com.example.product_service.api.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCode {
    CATEGORY_NOT_LEAF(409, "PRODUCT_001", "최하위 카테고리만 설정 가능합니다"),
    CANNOT_MODIFY_PRODUCT_OPTION_ON_SALE(409, "PRODUCT_002", "판매중인 상품은 옵션을 설정할 수 없습니다"),
    PRODUCT_NOT_FOUND(404, "PRODUCT_003", "상품을 찾을 수 없습니다"),
    CANNOT_MODIFY_PRODUCT_OPTION_HAS_VARIANTS(409, "PRODUCT_004", "상품에 상품 변형이 존재하면 상품 옵션을 변경할 수 없습니다"),
    EXCEED_PRODUCT_OPTION_COUNT(409, "PRODUCT_005", "상품에 설정할 수 있는 옵션의 개수는 최대 3개 입니다"),
    DUPLICATE_OPTION_TYPE(409, "PRODUCT_006", "동일한 상품 옵션은 설정할 수 없습니다"),
    NOT_MATCH_PRODUCT_OPTION_SPEC(409, "PRODUCT_007", "상품 옵션에 맞지 않는 옵션 값 Id 가 존재합니다"),
    PRODUCT_HAS_DUPLICATE_VARIANT(409, "PRODUCT_008", "동일한 옵션의 상품 변형이 존재합니다"),
    DUPLICATE_VARIANT_IN_REQUEST(400, "PRODUCT_009", "동일한 옵션의 상품 변형을 여러개 생성할 수 없습니다"),
    CANNOT_PUBLISH_DELETED_PRODUCT(409, "PRODUCT_010", "삭제된 상품은 게시할 수 없습니다"),
    NO_VARIANTS_TO_PUBLISH(409, "PRODUCT_011", "상품 변형이 없는 상품은 게시할 수 없습니다"),
    NO_THUMBNAIL_IMAGE(409, "PRODUCT_012", "대표 이미지가 없는 상품은 게시할 수 없습니다"),
    INVALID_DISPLAY_PRICE(409, "PRODUCT_013", "대표 가격이 유효하지 않은 상품은 게시할 수 없습니다"),
    PRODUCT_CATEGORY_REQUIRED(409, "PRODUCT_014", "상품은 반드시 하나의 카테고리에 속해야 합니다");
    private final int status;
    private final String code;
    private final String message;
}
