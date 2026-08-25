package com.example.order_service.order.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "order")
public record OrderProperties(
        int timeoutMinute
) {
}
