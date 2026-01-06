package com.example.order_service.api.common.client.payment;

import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class TossPaymentFeignConfig {
    @Value("${payment.toss.secret-key}")
    private String secretKey;

    @Bean
    public RequestInterceptor basicAuthRequestInterceptor() {
        return template -> {
            String authKey = secretKey + ":";
            String encodedKey = Base64.getEncoder().encodeToString(authKey.getBytes(StandardCharsets.UTF_8));
            template.header("Authorization", "Basic " + encodedKey);
        };
    }

    @Bean
    public ErrorDecoder tossPaymentErrorDecoder() {
        return new TossPaymentErrorDecoder();
    }
}
