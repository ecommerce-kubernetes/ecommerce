package com.example.order_service.order.infrastructure.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "order.topic")
public class SagaProperties {
    private String inventorySagaCommand;
    private String couponSagaCommand;
    private String pointsSagaCommand;
}
