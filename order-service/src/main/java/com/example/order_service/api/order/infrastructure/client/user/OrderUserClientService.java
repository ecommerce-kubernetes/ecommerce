package com.example.order_service.api.order.infrastructure.client.user;

import com.example.order_service.api.common.exception.NotFoundException;
import com.example.order_service.api.common.exception.server.InternalServerException;
import com.example.order_service.api.common.exception.server.UnavailableServiceException;
import com.example.order_service.api.order.infrastructure.client.user.dto.OrderUserResponse;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderUserClientService {
    private final OrderUserClient orderUserClient;

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserForOrderFallback")
    public OrderUserResponse getUserForOrder(Long userId){
        return orderUserClient.getOrderInfo(userId);
    }

    private OrderUserResponse getUserForOrderFallback(Long userId, Throwable throwable){
        if (throwable instanceof CallNotPermittedException){
            log.error("유저서비스가 응답하지 않아 서킷 브레이커가 열림");
            throw new UnavailableServiceException("유저 서비스가 응답하지 않습니다");
        }

        if (throwable instanceof NotFoundException){
            log.warn("유저를 찾을 수 없습니다");
            throw (NotFoundException) throwable;
        }

        throw new InternalServerException("유저 서비스에서 오류가 발생했습니다");
    }
}
