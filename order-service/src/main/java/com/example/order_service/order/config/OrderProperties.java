package com.example.order_service.order.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "order")
public record OrderProperties(
        int timeoutMinute
) {
}
