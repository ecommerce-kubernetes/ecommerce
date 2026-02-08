package com.example.order_service.api.order.domain.model;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum ProductStatus {
    PREPARING("PREPARING"),
    ON_SALE("ON_SALE"),
    STOP_SALE("STOP_SALE"),
    DELETED("DELETED"),

    UNKNOWN("UNKNOWN");
    private final String status;

    public static ProductStatus from (String status) {
        if (status == null || status.isBlank()) {
            return UNKNOWN;
        }

        return Arrays.stream(values())
                .filter(type -> type.name().equals(status.toUpperCase()))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
