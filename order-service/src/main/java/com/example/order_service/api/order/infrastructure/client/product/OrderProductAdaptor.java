package com.example.order_service.api.order.infrastructure.client.product;

import com.example.order_service.api.cart.infrastructure.client.dto.CartProductsRequest;
import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.ExternalServiceErrorCode;
import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse;
import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductsRequest;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderProductAdaptor {

    private final OrderProductClient orderProductClient;

    @CircuitBreaker(name = "productService", fallbackMethod = "getProductsFallback")
    public List<OrderProductResponse> getProducts(List<Long> variantIds) {
        OrderProductsRequest request = OrderProductsRequest.of(variantIds);
        return orderProductClient.getProductVariantByIds(request);
    }

    private List<OrderProductResponse> getProductsFallback(List<Long> variantIds, Throwable throwable){
        if(throwable instanceof CallNotPermittedException) {
            log.error("상품 서비스 장애로 서킷브레이커 열림");
            throw new BusinessException(ExternalServiceErrorCode.UNAVAILABLE);
        }

        if (throwable instanceof BusinessException) {
            throw (BusinessException) throwable;
        }

        throw new BusinessException(ExternalServiceErrorCode.SYSTEM_ERROR);
    }
}
