package com.example.product_service.service;

import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.response.product.ProductResponse;

public interface ProductService {
    ProductResponse saveProduct(ProductRequest request);
}
