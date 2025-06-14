package com.example.couponservice.advice.exceptions;

public class IsExistCouponException extends RuntimeException {
    public IsExistCouponException(String message) {
        super(message);
    }
}
