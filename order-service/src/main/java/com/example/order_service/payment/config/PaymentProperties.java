package com.example.order_service.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payment.timeout-minute")
public record PaymentProperties(
        int timeoutReady
) {
}
