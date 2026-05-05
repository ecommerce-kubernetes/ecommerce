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

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserInfoForOrderFallback")
    public UserClientResponse.UserInfo getUserInfoForOrder(Long userId) {
        return client.getUserInfoForOrder(userId);
    }
    
    private UserClientResponse.UserInfo getUserInfoForOrderFallback(Long userId, Throwable throwable) throws Throwable {
        throw translator.translate("USER-SERVICE", throwable);
    }
}
