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
    NOT_MATCH_PRODUCT_OPTION_SIZE(409, "PRODUCT_007", "상품 옵션 개수와 상품 변형의 옵션 값의 개수가 다릅니다"),
    PRODUCT_HAS_DUPLICATE_VARIANT(409, "PRODUCT_008", "동일한 옵션의 상품 변형이 존재합니다"),
    DUPLICATE_VARIANT_IN_REQUEST(400, "PRODUCT_009", "동일한 옵션의 상품 변형을 여러개 생성할 수 없습니다"),
    CANNOT_PUBLISH_DELETED_PRODUCT(409, "PRODUCT_010", "삭제된 상품은 게시할 수 없습니다"),
    NO_VARIANTS_TO_PUBLISH(409, "PRODUCT_011", "상품 변형이 없는 상품은 게시할 수 없습니다"),
    NO_THUMBNAIL_IMAGE(409, "PRODUCT_012", "대표 이미지가 없는 상품은 게시할 수 없습니다"),
    INVALID_DISPLAY_PRICE(409, "PRODUCT_013", "대표 가격이 유효하지 않은 상품은 게시할 수 없습니다"),
    PRODUCT_CATEGORY_REQUIRED(409, "PRODUCT_014", "상품은 반드시 하나의 카테고리에 속해야 합니다"),
    PRODUCT_VARIANT_DUPLICATE_OPTION(409, "PRODUCT_015", "동일한 옵션 값의 상품 변형을 생성할 수 없습니다"),
    NOT_MATCH_PRODUCT_OPTION_SPEC(409, "PRODUCT_016", "상품 옵션과 다른 옵션 값이 존재합니다"),
    CANNOT_DELETE_ALL_IMAGES_ON_SALE(409, "PRODUCT_017", "판매중인 상품은 이미지가 최소 한개 이상이여야 합니다"),
    INVALID_ORIGINAL_PRICE(409, "PRODUCT_018", "정가가 유효하지 않은 상품은 게시할 수 없습니다"),
    INVALID_DISCOUNT_RATE(409, "PRODUCT_019", "최대 할인율이 유효햐지 않은 상품은 게시할 수 없습니다"),
    DISPLAY_PRICE_GREATER_THAN_ORIGINAL(409, "PRODUCT_020", "판매가는 정가보다 비쌀 수 없습니다");
    private final int status;
    private final String code;
    private final String message;
}
