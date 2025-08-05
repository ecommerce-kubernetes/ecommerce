package com.example.product_service.controller.validation.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@TestConfiguration
public class ValidationTestConfig {
    @Bean
    public MessageSource messageSource(){
        ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
        ms.setBasename("classpath:messages");
        ms.setDefaultEncoding("UTF-8");
        return ms;
    }
    @Bean
    public LocalValidatorFactoryBean validator(MessageSource ms) {
        LocalValidatorFactoryBean vb = new LocalValidatorFactoryBean();
        vb.setValidationMessageSource(ms);
        return vb;
    }
}
