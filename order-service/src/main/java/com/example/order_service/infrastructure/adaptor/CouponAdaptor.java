package com.example.order_service.infrastructure.adaptor;

import com.example.order_service.common.exception.external.ExternalSystemException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.client.CouponFeignClient;
import com.example.order_service.infrastructure.dto.request.CouponClientRequest;
import com.example.order_service.infrastructure.dto.response.CouponClientResponse;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponAdaptor {
    private final CouponFeignClient client;

    @CircuitBreaker(name = "couponService", fallbackMethod = "calculateFallback")
    public CouponClientResponse.Calculate calculate(Long userId, Long couponId, Long totalPrice){
        CouponClientRequest.Calculate request = CouponClientRequest.Calculate.of(userId, couponId, totalPrice);
        return client.calculate(request);
    }

    private CouponClientResponse.Calculate calculateFallback(Long userId, Long couponId, Long totalAmount, Throwable throwable) throws Throwable {
        if (throwable instanceof CallNotPermittedException) {
            log.error("쿠폰 서비스 장애로 인해 서킷 브레이커 열림");
            throw new ExternalSystemUnavailableException("CircuitBreaker Open", throwable);
        }

        if (throwable instanceof ExternalSystemException) {
            throw throwable;
        }

        throw new ExternalSystemUnavailableException("쿠폰 서비스 통신 장애", throwable);
    }
}
