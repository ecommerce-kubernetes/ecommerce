package com.example.order_service.api.common.client.coupon;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class CouponFeignConfig {

    @Bean
    public ErrorDecoder couponErrorDecoder() {
        return new CouponErrorDecoder();
    }
}
