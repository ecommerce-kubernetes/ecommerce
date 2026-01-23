package com.example.order_service.api.order.infrastructure.client.user;

import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.ExternalServiceErrorCode;
import com.example.order_service.api.order.infrastructure.client.user.dto.OrderUserResponse;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderUserAdaptor {
    private final OrderUserClient orderUserClient;

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserForOrderFallback")
    public OrderUserResponse getUser(Long userId){
        return orderUserClient.getOrderInfo(userId);
    }

    private OrderUserResponse getUserForOrderFallback(Long userId, Throwable throwable){
        if (throwable instanceof CallNotPermittedException){
            log.error("유저서비스가 응답하지 않아 서킷 브레이커가 열림");
            throw new BusinessException(ExternalServiceErrorCode.UNAVAILABLE);
        }

        if (throwable instanceof BusinessException){
            throw (BusinessException) throwable;
        }

        throw new BusinessException(ExternalServiceErrorCode.SYSTEM_ERROR);
    }
}
