package com.example.couponservice.advice.exceptions;

public class InvalidCategoryCouponException extends RuntimeException {
    public InvalidCategoryCouponException(String message) {
        super(message);
    }
}
