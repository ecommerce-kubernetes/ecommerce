package com.example.order_service.service.client;

import com.example.order_service.client.ProductClient;
import com.example.order_service.common.MessageSourceUtil;
import com.example.order_service.exception.NotFoundException;
import com.example.order_service.service.client.dto.ProductResponse;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.example.order_service.common.MessagePath.PRODUCT_VARIANT_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductClientService {

    private final ProductClient productClient;
    private final MessageSourceUtil ms;

    @CircuitBreaker(name = "productService", fallbackMethod = "getProductFallback")
    public ProductResponse fetchProductByVariantId(Long productVariantId){
        return productClient.getProductVariant(productVariantId);
    }

    public List<ProductResponse> fetchProductByVariantIds(List<Long> productVariantIds){
        return productClient.getProductVariantByIds(productVariantIds);
    }

    //TODO 예외 클래스 변경
    public ProductResponse getProductFallback(Long productId, Throwable throwable){
        if(throwable instanceof CallNotPermittedException){
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Product Service unavailable"
            );
        }
        else if (throwable instanceof FeignException){
            if (((FeignException) throwable).status() == 404){
                throw new NotFoundException(ms.getMessage(PRODUCT_VARIANT_NOT_FOUND));
            }
        }
        throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Product Service Error"
        );
    }
}
