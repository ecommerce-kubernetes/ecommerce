package com.example.product_service.api.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CategoryErrorCode implements ErrorCode{
    CATEGORY_NOT_FOUND(404, "CATEGORY_001", "카테고리를 찾을 수 없습니다"),
    HAS_PRODUCT(409, "CATEGORY_002", "카테고리에 속한 상품이 존재합니다"),
    DUPLICATE_NAME(409, "CATEGORY_003", "상위 카테고리 내에 동일한 이름이 존재합니다"),
    EXCEED_MAX_DEPTH(409, "CATEGORY_004", "더이상 해당 부모 카테고리에는 자식 카테고리를 생성할 수 없습니다"),
    INVALID_INPUT_VALUE(400, "CATEGORY_005", "잘못된 입력값입니다"),
    HAS_CHILD(409, "CATEGORY_006", "해당 카테고리의 하위 카테고리가 존재합니다"),
    CANNOT_MOVE_TO_SELF(409, "CATEGORY_007", "잘못된 부모 설정입니다"),
    CATEGORY_ID_IS_NULL(500, "CATEGORY_008", "카테고리 id가 설정되지 않았습니다"),
    CANNOT_MOVE_TO_DESCENDANT(404, "CATEGORY_009", "카테고리를 자신의 하위로 이동할 수 없습니다");
    private final int status;
    private final String code;
    private final String message;
}
