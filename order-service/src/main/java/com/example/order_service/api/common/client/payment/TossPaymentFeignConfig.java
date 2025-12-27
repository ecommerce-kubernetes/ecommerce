package com.example.order_service.api.common.client.payment;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class TossPaymentFeignConfig {
    @Bean
    public ErrorDecoder tossPaymentErrorDecoder() {
        return new TossPaymentErrorDecoder();
    }
}
