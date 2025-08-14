package com.example.order_service.service.client;

import com.example.order_service.client.ProductClient;
import com.example.order_service.dto.client.ProductRequestIdsDto;
import com.example.order_service.dto.client.CompactProductResponseDto;
import com.example.order_service.dto.client.ProductResponseDto;
import com.example.order_service.exception.NotFoundException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductClientService {

    private final ProductClient productClient;

    @CircuitBreaker(name = "productService", fallbackMethod = "getProductFallback")
    public ProductResponseDto fetchProduct(Long productId){
        return productClient.getProduct(productId);
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "getProductListFallback")
    public List<CompactProductResponseDto> fetchProductBatch(ProductRequestIdsDto productRequestIdsDto){
        return productClient.getProductsByIdBatch(productRequestIdsDto);
    }

    public CompactProductResponseDto getProductFallback(Long productId, Throwable throwable){
        if(throwable instanceof CallNotPermittedException){
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Product Service unavailable"
            );
        }
        else if (throwable instanceof FeignException){
            if (((FeignException) throwable).status() == 404){
                throw new NotFoundException("Not Found Product");
            }
        }
        throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Product Service Error"
        );
    }

    public List<CompactProductResponseDto> getProductListFallback(ProductRequestIdsDto requestDto, Throwable throwable){
        if(throwable instanceof CallNotPermittedException){
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Product Service unavailable"
            );
        }
        else if (throwable instanceof FeignException){
            if (((FeignException) throwable).status() == 404){
                throw new NotFoundException("Not Found Product");
            }
        }
        throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Product Service Error"
        );
    }
}
