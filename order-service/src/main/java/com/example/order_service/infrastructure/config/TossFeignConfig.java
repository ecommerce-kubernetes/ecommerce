package com.example.order_service.infrastructure.config;

import com.example.order_service.infrastructure.config.properties.TossProperties;
import com.example.order_service.infrastructure.decoder.TossErrorDecoder;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RequiredArgsConstructor
public class TossFeignConfig {

    private final TossProperties properties;

    // 토스 요청시 헤더에 시크릿 인증 키 추가
    @Bean
    public RequestInterceptor tossAuthRequestInterceptor() {
        return template -> {
            String authKey = properties.getSecretKey() + ":";
            String encodedKey = Base64.getEncoder().encodeToString(authKey.getBytes(StandardCharsets.UTF_8));
            template.header("Authorization", "Basic " + encodedKey);
        };
    }

    @Bean
    public ErrorDecoder tossErrorDecoder(ObjectMapper objectMapper) {
        return new TossErrorDecoder(objectMapper);
    }
}
