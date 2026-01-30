package com.example.order_service.api.cart.infrastructure.client;

import com.example.order_service.api.cart.infrastructure.client.dto.CartProductResponse;
import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.ExternalServiceErrorCode;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CartProductAdaptor {

    private final CartProductClient cartProductClient;

    @CircuitBreaker(name = "productService", fallbackMethod = "getProductFallback")
    public CartProductResponse getProduct(Long productVariantId){
        return cartProductClient.getProductByVariantId(productVariantId);
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "getProductsFallback")
    public List<CartProductResponse> getProducts(List<Long> productVariantIds){
        return cartProductClient.getProductVariantByIds(productVariantIds);
    }

    private CartProductResponse getProductFallback(Long productVariantId, Throwable throwable){
        if(throwable instanceof CallNotPermittedException){
            log.error("상품 서비스 서킷 브레이커 열림");
            throw new BusinessException(ExternalServiceErrorCode.UNAVAILABLE);
        }

        if (throwable instanceof BusinessException){
            throw (BusinessException) throwable;
        }

        throw new BusinessException(ExternalServiceErrorCode.SYSTEM_ERROR);
    }

    private List<CartProductResponse> getProductsFallback(List<Long> productVariantIds, Throwable throwable){
        if(throwable instanceof CallNotPermittedException) {
            log.error("상품 서비스 장애로 서킷브레이커 열림");
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }
}
