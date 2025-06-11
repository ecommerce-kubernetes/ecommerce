package com.example.couponservice.advice.exceptions;

public class AlreadyUsedCouponException extends RuntimeException {
    public AlreadyUsedCouponException(String message) {
        super(message);
    }
}
