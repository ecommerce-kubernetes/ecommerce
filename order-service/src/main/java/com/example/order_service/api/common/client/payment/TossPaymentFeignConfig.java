package com.example.order_service.api.common.client.payment;

import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RequiredArgsConstructor
public class TossPaymentFeignConfig {

    private final TossProperties tossProperties;

    // 토스 요청시 헤더에 시크릿 키를 추가하는 인터셉터
    @Bean
    public RequestInterceptor basicAuthRequestInterceptor() {
        return template -> {
            String authKey = tossProperties.getSecretKey() + ":";
            String encodedKey = Base64.getEncoder().encodeToString(authKey.getBytes(StandardCharsets.UTF_8));
            template.header("Authorization", "Basic " + encodedKey);
        };
    }

    // 에러 디코더
    @Bean
    public ErrorDecoder tossPaymentErrorDecoder() {
        return new TossPaymentErrorDecoder();
    }
}
