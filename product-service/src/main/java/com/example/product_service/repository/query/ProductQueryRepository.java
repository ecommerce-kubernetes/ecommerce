package com.example.product_service.repository.query;

import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.product.ProductResponseDto;
import com.example.product_service.dto.response.product.ProductSummaryDto;
import com.example.product_service.entity.Products;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductQueryRepository {

    Page<ProductSummaryDto> findAllByProductSummaryProjection(String name, List<Long> categoryIds, Pageable pageable);
}
