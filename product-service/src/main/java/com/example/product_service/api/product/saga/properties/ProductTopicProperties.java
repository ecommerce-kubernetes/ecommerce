package com.example.product_service.api.product.saga.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "product.topics")
public class ProductTopicProperties {
    private String productSagaCommand;
    private String productSagaReply;
}
