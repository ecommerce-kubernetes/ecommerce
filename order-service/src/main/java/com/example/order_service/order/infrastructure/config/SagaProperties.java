package com.example.order_service.order.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "order.topics")
public record SagaProperties(
        String productSagaCommand,
        String couponSagaCommand,
        String userSagaCommand
) {
}
