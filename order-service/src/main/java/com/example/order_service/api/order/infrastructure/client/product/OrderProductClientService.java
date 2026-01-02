package com.example.order_service.api.order.infrastructure.client.product;

import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.CommonErrorCode;
import com.example.order_service.api.common.exception.server.InternalServerException;
import com.example.order_service.api.common.exception.server.UnavailableServiceException;
import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderProductClientService {

    private final OrderProductClient orderProductClient;

    @CircuitBreaker(name = "productService", fallbackMethod = "getProductsFallback")
    public List<OrderProductResponse> getProducts(List<Long> variantIds) {
        return orderProductClient.getProductVariantByIds(variantIds);
    }

    private List<OrderProductResponse> getProductsFallback(List<Long> variantIds, Throwable throwable){
        if(throwable instanceof CallNotPermittedException) {
            log.error("상품 서비스 장애로 서킷브레이커 열림");
            throw new BusinessException(CommonErrorCode.SERVICE_UNAVAILABLE);
        }
        throw new InternalServerException("상품 서비스에서 오류가 발생했습니다");
    }
}
