package com.example.product_service.service;

import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.response.product.ProductResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService{
    @Override
    public ProductResponse saveProduct(ProductRequest request) {
        return null;
    }
}
