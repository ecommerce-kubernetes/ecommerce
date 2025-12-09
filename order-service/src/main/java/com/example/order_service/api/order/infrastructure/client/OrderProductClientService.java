package com.example.order_service.api.order.infrastructure.client;

import com.example.order_service.api.common.exception.NotFoundException;
import com.example.order_service.api.common.exception.server.InternalServerException;
import com.example.order_service.api.common.exception.server.UnavailableServiceException;
import com.example.order_service.api.order.infrastructure.client.dto.OrderProductResponse;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderProductClientService {

    private final OrderProductClient orderProductClient;

    @CircuitBreaker(name = "productService", fallbackMethod = "getProductsFallback")
    public List<OrderProductResponse> getProducts(List<Long> productVariantIds) {
        List<OrderProductResponse> products = orderProductClient.getProductVariantByIds(productVariantIds);
        verifyMissingProducts(productVariantIds, products);
        return products;
    }

    private void verifyMissingProducts(List<Long> productVariantIds, List<OrderProductResponse> products){
        if (products.size() != productVariantIds.size()){
            Set<Long> responseIds = products.stream().map(OrderProductResponse::getProductVariantId).collect(Collectors.toSet());

            List<Long> missingIds = productVariantIds.stream()
                    .filter(id -> !responseIds.contains(id))
                    .toList();
            log.info("존재하지 않은 상품이 있습니다 missingIds = {}", missingIds);
            throw new NotFoundException("주문상품중 존재하지 않은 상품이 있습니다");
        }
    }

    private List<OrderProductResponse> getProductsFallback(List<Long> productVariantIds, Throwable throwable){
        if(throwable instanceof CallNotPermittedException) {
            log.error("상품 서비스 장애로 서킷브레이커 열림");
            throw new UnavailableServiceException("상품 서비스가 응답하지 않습니다");
        }
        throw new InternalServerException("상품 서비스에서 오류가 발생했습니다");
    }
}
