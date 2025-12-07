package com.example.order_service.api.cart.infrastructure.client;

import com.example.order_service.api.common.exception.server.InternalServerException;
import com.example.order_service.api.common.exception.server.UnavailableServiceException;
import com.example.order_service.api.common.exception.NotFoundException;
import com.example.order_service.api.cart.infrastructure.client.dto.CartProductResponse;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartProductClientService {

    private final CartProductClient cartProductClient;

    @CircuitBreaker(name = "productService", fallbackMethod = "getProductFallback")
    public CartProductResponse getProduct(Long productVariantId){
        return cartProductClient.getProductByVariantId(productVariantId);
    }

    public List<CartProductResponse> getProducts(List<Long> productVariantIds){
        return cartProductClient.getProductVariantByIds(productVariantIds);
    }

    private CartProductResponse getProductFallback(Long productVariantId, Throwable throwable){
        if(throwable instanceof CallNotPermittedException){
            throw new UnavailableServiceException(
                    "상품 서비스가 응답하지 않습니다"
            );
        }
        else if (throwable instanceof FeignException){
            if (((FeignException) throwable).status() == 404){
                throw new NotFoundException("상품을 찾을 수 없습니다");
            }
        }
        throw new InternalServerException(
                "상품 서비스에서 오류가 발생했습니다"
        );
    }
}
