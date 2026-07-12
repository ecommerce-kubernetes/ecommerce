package com.example.order_service.infrastructure.adaptor;

import com.example.order_service.infrastructure.client.ProductFeignClient;
import com.example.order_service.infrastructure.dto.command.ProductCommand;
import com.example.order_service.infrastructure.dto.request.ProductClientRequest;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 상품 도메인과의 통신을 담당하는 Adaptor
 * <p>
 * 상품 도메인 서비스 FeignClient 호출, 상품 도메인 서비스에 에러 발생시 서킷 브레이커를 통해 예외 전파를 관리
 * </p>
 *
 * @author 최민식
 * @since 2026. 06. 16
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductAdaptor {
    private final ProductFeignClient client;
    private final ExternalExceptionTranslator translator;

    /**
     * 상품 정보 목록을 조회
     * @param command 상품 조회 command
     * @return 상품 목록 정보
     */
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductsFallback")
    public ProductClientResponse.ProductList getProducts(ProductCommand.BulkSearch command) {
        ProductClientRequest.BulkSearch request = ProductClientRequest.BulkSearch.from(command.variantIds());
        return client.getProducts(request);
    }

    private ProductClientResponse.ProductList getProductsFallback(ProductCommand.BulkSearch command, Throwable throwable) throws Throwable {
        throw translator.translate("PRODUCT-SERVICE", throwable);
    }
}
