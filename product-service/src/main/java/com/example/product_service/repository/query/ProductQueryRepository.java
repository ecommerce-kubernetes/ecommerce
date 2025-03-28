package com.example.product_service.repository.query;

import com.example.product_service.dto.response.PageDto;
import com.example.product_service.entity.Products;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductQueryRepository {

    Page<Products> findAllByParameter(String name, Long categoryId, Pageable pageable);
}
