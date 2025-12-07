package com.example.order_service.api.cart.infrastructure.client;

import com.example.order_service.api.common.exception.server.InternalServerException;
import com.example.order_service.api.common.exception.server.UnavailableServiceException;
import com.example.order_service.api.common.exception.NotFoundException;
import com.example.order_service.api.cart.infrastructure.client.dto.CartProductResponse;
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
public class CartProductClientService {

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
            log.error("상품 서비스 장애로 서킷브레이커 열림");
            throw new UnavailableServiceException("상품 서비스가 응답하지 않습니다");
        }

        if (throwable instanceof NotFoundException){
            throw (NotFoundException) throwable;
        }

        throw new InternalServerException(throwable.getMessage());
    }

    private List<CartProductResponse> getProductsFallback(List<Long> productVariantIds, Throwable throwable){
        if(throwable instanceof CallNotPermittedException) {
            log.error("상품 서비스 장애로 서킷브레이커 열림");
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }
}
