package com.example.order_service.api.order.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod {
    CARD("카드"), VIRTUAL_ACCOUNT("가상계좌"), TRANSFER("계좌이체"), MOBILE_PHONE("휴대폰"),
    OTHER("기타");

    private final String method;

    public static PaymentMethod from (String status) {
        if (status == null || status.isBlank()) {
            return OTHER;
        }

        return Arrays.stream(values())
                .filter(type -> type.name().equals(status.toUpperCase()))
                .findFirst()
                .orElse(OTHER);
    }
}
