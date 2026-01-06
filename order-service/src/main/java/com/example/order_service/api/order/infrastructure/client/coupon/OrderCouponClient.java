package com.example.order_service.api.order.infrastructure.client.coupon;

import com.example.order_service.api.common.client.coupon.CouponFeignConfig;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponCalcRequest;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponDiscountResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "coupon-service", contextId = "orderCouponClient", configuration = CouponFeignConfig.class)
public interface OrderCouponClient {

    @PostMapping("/internal/coupons/calculate")
    OrderCouponDiscountResponse calculate(@RequestBody OrderCouponCalcRequest request);
}
