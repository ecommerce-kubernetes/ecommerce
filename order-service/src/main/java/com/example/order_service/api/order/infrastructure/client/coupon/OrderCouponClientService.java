package com.example.order_service.api.order.infrastructure.client.coupon;

import com.example.order_service.api.common.exception.NotFoundException;
import com.example.order_service.api.common.exception.server.InternalServerException;
import com.example.order_service.api.common.exception.server.UnavailableServiceException;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponCalcRequest;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponCalcResponse;
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
    public OrderCouponCalcResponse calculateDiscount(Long userId, Long couponId, Long totalPrice){
        OrderCouponCalcRequest request = OrderCouponCalcRequest.of(userId, couponId, totalPrice);
        return orderCouponClient.calculate(request);
    }

    private OrderCouponCalcResponse calculateDiscountFallback(Long userId, Long couponId, Long totalPrice, Throwable throwable){
        if (throwable instanceof CallNotPermittedException) {
            throw new UnavailableServiceException("쿠폰 서비스가 응답하지 않습니다");
        }

        if (throwable instanceof NotFoundException) {
            throw (NotFoundException) throwable;
        }

        throw new InternalServerException("쿠폰 서비스에서 오류가 발생했습니다");
    }

}
