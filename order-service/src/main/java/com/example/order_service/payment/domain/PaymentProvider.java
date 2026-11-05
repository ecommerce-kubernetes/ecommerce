package com.example.order_service.payment.domain;

public enum PaymentProvider {
    TOSS, KAKAO, UNKNOWN;

    public static PaymentProvider from(String provider) {
        return switch (provider) {
            case "TOSS" -> PaymentProvider.TOSS;
            case "KAKAO" -> PaymentProvider.KAKAO;
            default -> PaymentProvider.UNKNOWN;
        };
    }
}
