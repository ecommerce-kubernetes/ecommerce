package com.example.order_service.infrastructure.client;

import com.example.order_service.infrastructure.config.DefaultFeignConfig;
import com.example.order_service.infrastructure.dto.request.ItemCouponsRequest;
import com.example.order_service.infrastructure.dto.response.coupon.CartCouponResponse;
import com.example.order_service.infrastructure.dto.response.coupon.ItemCouponResponse;
import com.example.order_service.infrastructure.dto.response.coupon.ItemCouponsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "coupon-service", contextId = "couponClient", configuration = DefaultFeignConfig.class)
public interface CouponFeignClient {

    @GetMapping("/internal/users/{userId}/item-coupons/{itemCouponId}")
    ItemCouponResponse getItemCoupon(@PathVariable("userId") Long userId,
                                     @PathVariable("itemCouponId") Long itemCouponId);

    @PostMapping("/internal/users/{userId}/item-coupons")
    ItemCouponsResponse getItemCoupons(@PathVariable("userId") Long userId,
                                       @RequestBody ItemCouponsRequest request);

    @GetMapping("/internal/users/{userId}/cart-coupons/{cartCouponId}")
    CartCouponResponse getCartCoupon(@PathVariable("userId") Long userId,
                                     @PathVariable("cartCouponId") Long cartCouponId);
}
