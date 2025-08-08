package com.example.product_service.service;

import com.example.product_service.dto.ProductSearch;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.product.ProductResponse;
import com.example.product_service.dto.response.product.ProductSummaryResponse;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    ProductResponse saveProduct(ProductRequest request);
    PageDto<ProductSummaryResponse> getProducts(Pageable pageable, ProductSearch search);
}
