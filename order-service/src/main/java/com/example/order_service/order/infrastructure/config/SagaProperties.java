package com.example.order_service.order.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "order.topic")
public class SagaProperties {
}
