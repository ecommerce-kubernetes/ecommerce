package com.example.product_service.api.product.service;

import com.example.product_service.api.product.service.dto.command.ProductCreateCommand;
import com.example.product_service.api.product.service.dto.result.ProductCreateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    public ProductCreateResponse createProduct(ProductCreateCommand command) {
        return null;
    }
}
