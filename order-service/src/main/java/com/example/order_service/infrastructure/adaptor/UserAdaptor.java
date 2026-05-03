package com.example.order_service.infrastructure.adaptor;

import com.example.order_service.common.exception.external.ExternalSystemException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.client.UserFeignClient;
import com.example.order_service.infrastructure.dto.response.UserClientResponse;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserAdaptor {
    private final UserFeignClient client;

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserInfoForOrderFallback")
    public UserClientResponse.OrderUserInfo getUserInfoForOrder(Long userId) {
        return client.getUserInfoForOrder(userId);
    }
    
    private UserClientResponse.OrderUserInfo getUserInfoForOrderFallback(Long userId, Throwable throwable) throws Throwable {
        if (throwable instanceof CallNotPermittedException) {
            log.error("유저 서비스 장애로 인해 서킷 브레이커 열림");
            throw new ExternalSystemUnavailableException("CircuitBreaker Open", throwable);
        }
        
        //에러 디코더에서 던져진 에러
        if (throwable instanceof ExternalSystemException) {
            throw throwable;
        }
        
        // 에러 디코더를 타지 못한 에러 (타임아웃, 연결 오류, 파싱 등)
        throw new ExternalSystemUnavailableException("유저 서비스 통신 장애", throwable);
    }
}
