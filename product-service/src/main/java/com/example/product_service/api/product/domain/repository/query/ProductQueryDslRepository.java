package com.example.product_service.api.product.domain.repository.query;

import com.example.product_service.api.product.controller.dto.ProductSearchCondition;
import com.example.product_service.api.product.domain.model.Product;
import org.springframework.data.domain.Page;

public interface ProductQueryDslRepository {
    Page<Product> findProductsByCondition(ProductSearchCondition condition);
}
