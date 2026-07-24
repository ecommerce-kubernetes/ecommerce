package com.example.order_service.infrastructure.adaptor;

import com.example.order_service.infrastructure.client.ProductFeignClient;
import com.example.order_service.infrastructure.dto.request.ProductBulkSearchRequest;
import com.example.order_service.infrastructure.dto.response.product.ProductResponse;
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
    private final ExternalExceptionTranslator translator;

    @CircuitBreaker(name = "productService", fallbackMethod = "getProductsFallback")
    public ProductResponse getProducts(List<Long> productVariantIds) {
        ProductBulkSearchRequest request = ProductBulkSearchRequest.builder()
                .productVariantId(productVariantIds)
                .build();
        return client.getProducts(request);
    }

    private ProductResponse getProductsFallback(List<Long> productVariantId, Throwable throwable) throws Throwable {
        throw translator.translate("PRODUCT-SERVICE", throwable);
    }
}
