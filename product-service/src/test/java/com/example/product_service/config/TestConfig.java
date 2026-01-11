package com.example.product_service.config;

import com.example.product_service.controller.util.specification.config.SwaggerProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestConfig {

    @Bean
    public SwaggerProperties swaggerProperties() {
        return new SwaggerProperties(); // 기본 생성자만 있으면 충분
    }

}
