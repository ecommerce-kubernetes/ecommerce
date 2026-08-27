package com.example.order_service.saga.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "saga.timeout-minute")
public record SagaProperties(
        int timeoutForwardPending,
        int timeoutCompensatePending
) {
}
