package com.example.order_service.infrastructure.adaptor;

import com.example.order_service.infrastructure.client.UserFeignClient;
import com.example.order_service.infrastructure.dto.response.UserClientResponse;
import com.example.order_service.infrastructure.dto.response.user.UserProfileResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 유저 도메인과의 통신을 담당하는 Adaptor
 * <p>
 * 유저 도메인 서비스 FeignClient 호출, 유저 도메인 서비스에 에러 발생시 서킷 브레이커를 통해 예외 전파를 관리
 * </p>
 *
 * @author 최민식
 * @since 2026. 06. 16
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserAdaptor {
    private final UserFeignClient client;
    private final ExternalExceptionTranslator translator;

    @Deprecated
    @CircuitBreaker(name = "userService", fallbackMethod = "getUserProfileFallback")
    public UserClientResponse.Profile getUserProfileDeprecated(Long userId) {
        return client.getUserProfile(userId);
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserPointsFallback")
    public UserClientResponse.UserPoints getUserPoints(Long userId) {
        return client.getUserPoints(userId);
    }

    public UserProfileResponse getUserProfile(Long userId){
        return null;
    }

    private UserClientResponse.Profile getUserProfileFallback(Long userId, Throwable throwable) throws Throwable {
        throw translator.translate("USER-SERVICE", throwable);
    }

    private UserClientResponse.UserPoints getUserPointsFallback(Long userId, Throwable throwable) throws Throwable {
        throw translator.translate("USER-SERVICE", throwable);
    }
}
