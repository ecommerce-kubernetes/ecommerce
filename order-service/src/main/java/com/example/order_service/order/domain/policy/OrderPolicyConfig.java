package com.example.order_service.order.domain.policy;

import com.example.order_service.order.infrastructure.config.OrderSheetProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class OrderPolicyConfig {

    private final OrderSheetProperties properties;

    @Bean
    public PointUsagePolicy pointUsagePolicy() {
        return new DefaultPointUsagePolicy(properties.pointLimitRate());
    }
}
