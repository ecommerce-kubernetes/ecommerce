package com.example.order_service.infrastructure.config;

import com.example.order_service.infrastructure.decoder.DefaultErrorDecoder;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class DefaultFeignConfig {

    @Bean
    public ErrorDecoder defaultErrorDecoder(ObjectMapper objectMapper) {
        return new DefaultErrorDecoder(objectMapper);
    }
}
