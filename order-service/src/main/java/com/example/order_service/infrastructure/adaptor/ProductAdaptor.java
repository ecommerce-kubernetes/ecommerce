package com.example.order_service.infrastructure.adaptor;

import com.example.order_service.infrastructure.client.ProductFeignClient;
import com.example.order_service.infrastructure.dto.command.ProductCommand;
import com.example.order_service.infrastructure.dto.request.ProductClientRequest;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductAdaptor {
    private final ProductFeignClient client;
    private final ExternalExceptionTranslator translator;

    @CircuitBreaker(name = "productService", fallbackMethod = "getProductsForOrderFallback")
    public ProductClientResponse.ProductList getProductsForOrder(ProductCommand.Validate command) {
        ProductClientRequest.Validate request = ProductClientRequest.Validate.from(command);
        return client.getProductsForOrder(request);
    }

    private ProductClientResponse.ProductList getProductsForOrderFallback(ProductCommand.Validate command, Throwable throwable) throws Throwable {
        throw translator.translate("PRODUCT-SERVICE", throwable);
    }
}
