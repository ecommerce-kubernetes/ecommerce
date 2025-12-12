package com.example.order_service.service.client;

import com.example.order_service.api.common.exception.InsufficientException;
import com.example.order_service.api.common.exception.NotFoundException;
import com.example.order_service.client.CouponClient;
import com.example.order_service.common.MessageSourceUtil;
import com.example.order_service.service.client.dto.CouponResponse;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static com.example.order_service.common.MessagePath.ALREADY_USED_COUPON;
import static com.example.order_service.common.MessagePath.PRODUCT_VARIANT_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CouponClientService {
    private final CouponClient couponClient;
    private final MessageSourceUtil ms;

    @CircuitBreaker(name = "couponService", fallbackMethod = "getCouponFallback")
    public CouponResponse fetchCouponByUserCouponId(Long userId, Long userCouponId){
        return couponClient.getCoupon(userId, userCouponId);
    }

    public CouponResponse getCouponFallback(Long userId, Long userCouponId, Throwable throwable){
        if(throwable instanceof CallNotPermittedException){
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Coupon Service unavailable"
            );
        }
        else if (throwable instanceof FeignException){
            if (((FeignException) throwable).status() == 404){
                throw new NotFoundException(ms.getMessage(PRODUCT_VARIANT_NOT_FOUND));
            } else if (((FeignException) throwable).status() == 409){
                throw new InsufficientException(ms.getMessage(ALREADY_USED_COUPON));
            }
        }

        throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Coupon Service Error"
        );
    }
}
