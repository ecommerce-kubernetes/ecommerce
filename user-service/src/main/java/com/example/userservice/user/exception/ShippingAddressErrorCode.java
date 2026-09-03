package com.example.userservice.user.exception;

import com.example.userservice.common.exception.ErrorCategory;
import com.example.userservice.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ShippingAddressErrorCode implements ErrorCode {
    SHIPPING_ADDRESS_NOT_FOUND(ErrorCategory.NOT_FOUND, "SHIPPING_ADDRESS_NOT_FOUND", "배송지를 찾을 수 없습니다");
    private final ErrorCategory category;
    private final String code;
    private final String message;
}
