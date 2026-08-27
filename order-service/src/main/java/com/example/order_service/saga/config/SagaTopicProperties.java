package com.example.order_service.saga.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "order.topics")
public record SagaTopicProperties(
        String productSagaCommand,
        String couponSagaCommand,
        String userSagaCommand,
        String orderSagaReply
) {
}
