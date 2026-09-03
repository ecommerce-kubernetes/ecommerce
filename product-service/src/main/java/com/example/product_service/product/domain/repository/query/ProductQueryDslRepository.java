package com.example.product_service.product.domain.repository.query;

import com.example.product_service.product.domain.model.Product;
import com.example.product_service.product.application.service.dto.command.ProductCommand;
import org.springframework.data.domain.Page;

public interface ProductQueryDslRepository {
    Page<Product> findProductsByCondition(ProductCommand.Search condition);
}
