package com.example.couponservice.advice.exceptions;

public class IsExpiredCouponException extends RuntimeException {
    public IsExpiredCouponException(String message) {
        super(message);
    }
}
