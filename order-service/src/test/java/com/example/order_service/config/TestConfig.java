package com.example.order_service.config;

import com.example.order_service.api.common.config.SwaggerProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestConfig {

    @Bean
    public SwaggerProperties swaggerProperties() {
        return new SwaggerProperties();
    }
}
