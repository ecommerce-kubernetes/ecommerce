package com.example.userservice.support.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;

@TestConfiguration
public class MockKafkaConfig {
    @Bean
    @Primary
    public KafkaTemplate<String, String> mockKafkaTemplate() {
        return Mockito.mock(KafkaTemplate.class);
    }
}
