package com.example.product_service.category.exception;

import com.example.product_service.common.exception.ErrorCategory;
import com.example.product_service.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CategoryErrorCode implements ErrorCode {
    EXCEED_MAX_DEPTH(ErrorCategory.BUSINESS_CONFLICT, "EXCEED_MAX_DEPTH", "더이상 해당 부모 카테고리에는 자식 카테고리를 생성할 수 없습니다"),
    CANNOT_SET_SELF_AS_PARENT(ErrorCategory.BUSINESS_CONFLICT, "CANNOT_SET_SELF_AS_PARENT", "자기 자신을 부모 카테고리로 설정할 수 없습니다."),


    CATEGORY_NOT_FOUND(ErrorCategory.NOT_FOUND, "CATEGORY_001", "카테고리를 찾을 수 없습니다"),
    HAS_PRODUCT(ErrorCategory.BUSINESS_CONFLICT, "CATEGORY_002", "카테고리에 속한 상품이 존재합니다"),
    DUPLICATE_NAME(ErrorCategory.BUSINESS_CONFLICT, "CATEGORY_003", "상위 카테고리 내에 동일한 이름이 존재합니다"),
    INVALID_INPUT_VALUE(ErrorCategory.INVALID_REQUEST, "CATEGORY_005", "잘못된 입력값입니다"),
    HAS_CHILD(ErrorCategory.BUSINESS_CONFLICT, "CATEGORY_006", "해당 카테고리의 하위 카테고리가 존재합니다"),
    CATEGORY_ID_IS_NULL(ErrorCategory.SYSTEM_ERROR, "CATEGORY_008", "카테고리 id가 설정되지 않았습니다"),
    CANNOT_SET_DESCENDANT(ErrorCategory.NOT_FOUND, "CATEGORY_009", "카테고리를 자신의 하위로 이동할 수 없습니다");
    private final ErrorCategory category;
    private final String code;
    private final String message;
}
