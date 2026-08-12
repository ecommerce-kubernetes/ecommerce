package com.example.order_service.order.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "order.topics")
public record SagaProperties(
        String productSagaCommand,
        String couponSagaCommand,
        String userSagaCommand
) {
}
