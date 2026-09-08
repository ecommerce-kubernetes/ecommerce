package com.example.product_service.category.adapter.out.client;

import com.example.product_service.category.application.port.CategoryProductPort;
import org.springframework.stereotype.Component;

@Component
public class CategoryProductAdapter implements CategoryProductPort {
    @Override
    public boolean existsProductForCategory(Long categoryId) {
        return false;
    }
}
