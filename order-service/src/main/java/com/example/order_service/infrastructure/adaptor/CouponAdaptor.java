package com.example.order_service.infrastructure.adaptor;

import com.example.order_service.infrastructure.client.CouponFeignClient;
import com.example.order_service.infrastructure.dto.request.CouponClientRequest;
import com.example.order_service.infrastructure.dto.response.CouponClientResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponAdaptor {
    private final CouponFeignClient client;
    private final ExternalExceptionTranslator translator;

    @CircuitBreaker(name = "couponService", fallbackMethod = "calculateFallback")
    public CouponClientResponse.Calculate calculate(Long userId, Long couponId, Long totalPrice){
        CouponClientRequest.Calculate request = CouponClientRequest.Calculate.of(userId, couponId, totalPrice);
        return client.calculate(request);
    }

    private CouponClientResponse.Calculate calculateFallback(Long userId, Long couponId, Long totalAmount, Throwable throwable) throws Throwable {
        throw translator.translate("COUPON-SERVICE", throwable);
    }
}
