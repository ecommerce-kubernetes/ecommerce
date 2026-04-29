package com.example.order_service.infrastructure.config;

import com.example.order_service.infrastructure.decoder.ProductErrorDecoder;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class ProductFeignConfig {

    @Bean
    public ErrorDecoder productErrorDecoder(ObjectMapper objectMapper) {
        return new ProductErrorDecoder(objectMapper);
    }
}
