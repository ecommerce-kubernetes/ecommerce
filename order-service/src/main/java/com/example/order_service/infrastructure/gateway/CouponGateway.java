package com.example.order_service.infrastructure.gateway;

import com.example.order_service.infrastructure.client.CouponFeignClient;
import com.example.order_service.infrastructure.dto.request.ItemCouponsRequest;
import com.example.order_service.infrastructure.dto.response.coupon.CartCouponResponse;
import com.example.order_service.infrastructure.dto.response.coupon.ItemCouponResponse;
import com.example.order_service.infrastructure.dto.response.coupon.ItemCouponsResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponGateway {
    private final CouponFeignClient client;
    private final ExternalExceptionTranslator translator;

    @CircuitBreaker(name = "couponService", fallbackMethod = "getItemCouponFallback")
    public ItemCouponResponse getItemCoupon(Long userId, Long itemCouponId) {
        return client.getItemCoupon(userId, itemCouponId);
    }

    private ItemCouponResponse getItemCouponFallback(Long userId, Long itemCouponId, Throwable throwable) throws Throwable {
        throw translator.translate("COUPON-SERVICE", throwable);
    }

    @CircuitBreaker(name = "couponService", fallbackMethod = "getItemCouponsFallback")
    public ItemCouponsResponse getItemCoupons(Long userId, List<Long> itemCouponIds) {
        ItemCouponsRequest request = ItemCouponsRequest.builder().itemCouponIds(itemCouponIds).build();
        return client.getItemCoupons(userId, request);
    }

    private ItemCouponsResponse getItemCouponsFallback(Long userId, List<Long> itemCouponIds, Throwable throwable) throws Throwable {
        throw translator.translate("COUPON-SERVICE", throwable);
    }


    @CircuitBreaker(name = "couponService", fallbackMethod = "getCartCouponFallback")
    public CartCouponResponse getCartCoupon(Long userId, Long cartCouponId) {
        return client.getCartCoupon(userId, cartCouponId);
    }

    private CartCouponResponse getCartCouponFallback(Long userId, Long cartCouponId, Throwable throwable) throws Throwable {
        throw translator.translate("COUPON-SERVICE", throwable);
    }

}
