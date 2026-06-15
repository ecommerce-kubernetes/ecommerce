package com.example.order_service.infrastructure.adaptor;

import com.example.order_service.infrastructure.client.UserFeignClient;
import com.example.order_service.infrastructure.dto.response.UserClientResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserAdaptor {
    private final UserFeignClient client;
    private final ExternalExceptionTranslator translator;

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserProfileFallback")
    public UserClientResponse.Profile getUserProfile(Long userId) {
        return client.getUserProfile(userId);
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserPointsFallback")
    public UserClientResponse.UserPoints getUserPoints(Long userId) {
        return client.getUserPoints(userId);
    }

    private UserClientResponse.Profile getUserProfileFallback(Long userId, Throwable throwable) throws Throwable {
        throw translator.translate("USER-SERVICE", throwable);
    }

    private UserClientResponse.UserPoints getUserPointsFallback(Long userId, Throwable throwable) throws Throwable {
        throw translator.translate("USER-SERVICE", throwable);
    }
}
