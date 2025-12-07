package com.example.order_service.api.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@Profile("!test-mock")
@EnableScheduling
public class SchedulerConfig {
}
