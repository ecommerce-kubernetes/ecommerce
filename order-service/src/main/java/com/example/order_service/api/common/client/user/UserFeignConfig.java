package com.example.order_service.api.common.client.user;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class UserFeignConfig {

    @Bean
    public ErrorDecoder userErrorDecoder(){
        return new UserErrorDecoder();
    }
}
