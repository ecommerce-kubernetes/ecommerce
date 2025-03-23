package com.example.product_service.service;

import com.example.product_service.dto.request.ProductRequestDto;
import com.example.product_service.dto.response.ProductResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService{

    @Override
    public ProductResponseDto createProduct(ProductRequestDto productRequestDto) {
        return null;
    }
}
