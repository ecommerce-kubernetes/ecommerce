package com.example.order_service.api.order.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    READY("READY"), IN_PROGRESS("IN_PROGRESS"), WAITING_FOR_DEPOSIT("WAITING_FOR_DEPOSIT"),
    DONE("DONE"), CANCELLED("CANCELLED"), ABORT("ABORT"), EXPIRED("EXPIRED"),
    UNKNOWN("알 수 없음");

    private final String status;

    public static PaymentStatus from (String status) {
        if (status == null || status.isBlank()) {
            return UNKNOWN;
        }

        return Arrays.stream(values())
                .filter(type -> type.name().equals(status.toUpperCase()))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
