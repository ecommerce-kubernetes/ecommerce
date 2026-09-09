package com.example.product_service.product.application.port;

import com.example.product_service.product.application.port.dto.ProductCategoryResult;

public interface ProductCategoryPort {
    ProductCategoryResult getCategory(Long categoryId);
}
