package com.example.product_service.repository.query;

import com.example.product_service.dto.response.product.ProductSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductQueryRepository {

    Page<ProductSummaryDto> findAllByProductSummaryProjection(String name, List<Long> categoryIds, Integer rating, Pageable pageable);
    Page<ProductSummaryDto> findAllByCategorySortDiscount(List<Long> categoryIds, Pageable pageable);
    Page<ProductSummaryDto> findPopularProductByCategory(List<Long> categoryIds, double ratingAvg, int minimumReviewCount, Pageable pageable);
    double allProductRatingAvg(List<Long> categoryIds);
}
