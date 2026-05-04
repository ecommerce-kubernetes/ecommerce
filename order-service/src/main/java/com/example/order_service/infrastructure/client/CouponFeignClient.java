package com.example.order_service.infrastructure.client;

import com.example.order_service.infrastructure.config.DefaultFeignConfig;
import com.example.order_service.infrastructure.dto.request.CouponClientRequest;
import com.example.order_service.infrastructure.dto.response.CouponClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "coupon-service", contextId = "couponClient", configuration = DefaultFeignConfig.class)
public interface CouponFeignClient {

    @PostMapping("/internal/coupons/calculate")
    CouponClientResponse.Calculate calculate(@RequestBody CouponClientRequest.Calculate request);

}
