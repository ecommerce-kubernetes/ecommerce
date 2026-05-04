package com.example.order_service.infrastructure.adaptor;

import com.example.order_service.common.exception.external.ExternalSystemException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.client.ProductFeignClient;
import com.example.order_service.infrastructure.dto.request.ProductClientRequest;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductAdaptor {

    private final ProductFeignClient client;

    @CircuitBreaker(name = "productService", fallbackMethod = "getProductsByVariantIdsFallback")
    public List<ProductClientResponse.Product> getProductsByVariantIds(List<Long> productVariantIds){
        ProductClientRequest.ProductVariantIds request = ProductClientRequest.ProductVariantIds.of(productVariantIds);
        return client.getProductsByVariantIds(request);
    }

    private List<ProductClientResponse.Product> getProductsByVariantIdsFallback(List<Long> productVariantIds, Throwable throwable) throws Throwable {
        // 서킷 브레이커가 열린 경우
        if (throwable instanceof CallNotPermittedException) {
            log.error("상품 서비스 장애로 인해 서킷 브레이커 열림");
            throw new ExternalSystemUnavailableException("CIRCUIT_BREAKER_OPEN", "상품 서비스 서킷 브레이커 열림", throwable);
        }

        // 에러 디코더에서 던져진 에러
        if (throwable instanceof ExternalSystemException) {
            throw throwable;
        }

        // 에러 디코더를 타지 못한 에러 (타임아웃, 연결 오류 등)
        throw new ExternalSystemUnavailableException("SERVICE_UNAVAILABLE", "상품 서비스 통신 장애", throwable);
    }
}
