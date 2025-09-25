package com.example.order_service.client;

import com.example.order_service.service.client.dto.CouponResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "coupon-service")
public interface CouponClient {
    @GetMapping("/coupon/{userId}/{userCouponId}")
    CouponResponse getCoupon(@PathVariable("userId") Long userId, @PathVariable("userCouponId") Long userCouponId);

}
