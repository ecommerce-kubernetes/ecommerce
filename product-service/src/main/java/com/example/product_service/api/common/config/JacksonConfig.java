package com.example.product_service.api.common.config;

import com.example.product_service.api.common.util.StringTrimmerDeserializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder.deserializerByType(String.class, new StringTrimmerDeserializer());
    }
}
