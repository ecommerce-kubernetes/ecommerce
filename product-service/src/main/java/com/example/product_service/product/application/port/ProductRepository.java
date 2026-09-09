package com.example.product_service.product.application.port;

import com.example.product_service.product.domain.Product;

public interface ProductRepository {
    Product save(Product product);
}
