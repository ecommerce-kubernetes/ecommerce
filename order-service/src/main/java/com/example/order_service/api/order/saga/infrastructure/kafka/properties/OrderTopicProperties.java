package com.example.order_service.api.order.saga.infrastructure.kafka.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "order.topics")
public class OrderTopicProperties {
    private String productSagaCommand;
    private String productSagaReply;
    private String couponSagaCommand;
    private String couponSagaReply;
    private String userSagaCommand;
    private String userSagaReply;
}
