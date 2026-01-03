package com.example.order_service.api.order.infrastructure.client.coupon;

import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.ExternalServiceErrorCode;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponCalcRequest;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponDiscountResponse;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCouponClientService {
    private final OrderCouponClient orderCouponClient;

    @CircuitBreaker(name = "couponService", fallbackMethod = "calculateDiscountFallback")
    public OrderCouponDiscountResponse calculateDiscount(Long userId, Long couponId, Long totalPrice){
        OrderCouponCalcRequest request = OrderCouponCalcRequest.of(userId, couponId, totalPrice);
        return orderCouponClient.calculate(request);
    }

    private OrderCouponDiscountResponse calculateDiscountFallback(Long userId, Long couponId, Long totalPrice, Throwable throwable){
        if (throwable instanceof CallNotPermittedException) {
            log.warn("쿠폰 서비스 서킷 브레이커 열림");
            throw new BusinessException(ExternalServiceErrorCode.UNAVAILABLE);
        }

        if (throwable instanceof BusinessException) {
            throw (BusinessException) throwable;
        }

        throw new BusinessException(ExternalServiceErrorCode.SYSTEM_ERROR);
    }

}
