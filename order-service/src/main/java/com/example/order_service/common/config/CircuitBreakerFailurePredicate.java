package com.example.order_service.common.config;

import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.common.exception.business.ErrorCode;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import feign.RetryableException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

//TODO 삭제 공통 decoder 사용으로 마이그레이션
public class CircuitBreakerFailurePredicate implements Predicate<Throwable> {
    @Override
    public boolean test(Throwable throwable) {
        if (throwable instanceof BusinessException) {
            ErrorCode errorCode = ((BusinessException) throwable).getErrorCode();
            return errorCode.getStatus() >= 500;
        }

        if (throwable instanceof ExternalClientException) {
            return false;
        }
        if (throwable instanceof ExternalServerException || throwable instanceof ExternalSystemUnavailableException) {
            return true;
        }

        if (throwable instanceof IOException ||
                throwable instanceof TimeoutException ||
                throwable instanceof RetryableException) {
            return true;
        }

        if (throwable instanceof CallNotPermittedException) {
            return false;
        }

        return true;
    }
}
