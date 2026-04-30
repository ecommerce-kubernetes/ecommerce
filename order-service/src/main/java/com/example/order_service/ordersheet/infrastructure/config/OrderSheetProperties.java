package com.example.order_service.ordersheet.infrastructure.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "order.sheet")
public record OrderSheetProperties(
        @Min(value = 1) long ttlMinutes
) {
}
