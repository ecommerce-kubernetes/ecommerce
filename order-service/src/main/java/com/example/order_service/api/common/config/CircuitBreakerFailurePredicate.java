package com.example.order_service.api.common.config;

import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.ErrorCode;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

public class CircuitBreakerFailurePredicate implements Predicate<Throwable> {
    @Override
    public boolean test(Throwable throwable) {
        if (throwable instanceof BusinessException) {
            ErrorCode errorCode = ((BusinessException) throwable).getErrorCode();
            return errorCode.getStatus() >= 500;
        }

        if (throwable instanceof IOException || throwable instanceof TimeoutException) {
            return true;
        }

        if (throwable instanceof CallNotPermittedException) {
            return false;
        }

        return false;
    }
}
