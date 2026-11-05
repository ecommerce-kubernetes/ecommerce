package com.example.userservice.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "outbox.sweep")
public record OutboxSweepProperties(
        Duration thresholdSecond
) {
}
