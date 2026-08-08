package com.example.order_service.payment.application.port.dto;

public enum PaymentPGStatus {
    WAITING_FOR_DEPOSIT, DONE, CANCELED, PARTIAL_CANCELED, ABORT, EXPIRED, UNKNOWN
}
