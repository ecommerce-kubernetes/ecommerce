package com.example.order_service.service.client;

import com.example.order_service.client.UserClient;
import com.example.order_service.common.MessageSourceUtil;
import com.example.order_service.api.common.exception.NotFoundException;
import com.example.order_service.service.client.dto.UserBalanceResponse;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static com.example.order_service.common.MessagePath.PRODUCT_VARIANT_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserClientService {
    private final UserClient userClient;
    private final MessageSourceUtil ms;

    @CircuitBreaker(name = "userService", fallbackMethod = "fetchBalanceFallback")
    public UserBalanceResponse fetchBalance(){
        return userClient.getUserBalance();
    }

    public UserBalanceResponse fetchBalanceFallback(Throwable throwable){
        log.info("{}", throwable.getMessage());
        if(throwable instanceof CallNotPermittedException){
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "User Service Unavailable"
            );
        } else if (throwable instanceof FeignException){
            if (((FeignException) throwable).status() == 404){
                throw new NotFoundException(ms.getMessage(PRODUCT_VARIANT_NOT_FOUND));
            }
        }
        throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "User Service Error"
        );
    }
}
