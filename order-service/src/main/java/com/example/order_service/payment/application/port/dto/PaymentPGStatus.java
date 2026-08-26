package com.example.order_service.payment.application.port.dto;

public enum PaymentPGStatus {
    READY, IN_PROGRESS, WAITING_FOR_DEPOSIT, DONE, CANCELED, PARTIAL_CANCELED, ABORTED, EXPIRED, UNKNOWN
}
