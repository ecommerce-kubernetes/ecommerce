package com.example.config_service;

import org.springframework.cloud.bootstrap.encrypt.RsaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RsaConfig {

    @Bean
    public RsaProperties rsaProperties() {
        return new RsaProperties();
    }
}
