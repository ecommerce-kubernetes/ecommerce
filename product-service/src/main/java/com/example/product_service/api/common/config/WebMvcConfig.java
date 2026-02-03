package com.example.product_service.api.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry resourceHandlerRegistry) {
        ResourceHandlerRegistration resourceHandlerRegistration = resourceHandlerRegistry.addResourceHandler("/docs/**")
                .addResourceLocations(
                        "classpath:/static/docs/",
                        "file:build/resources/main/static/docs/"
                );
    }
}
