package com.example.product_service.repository.query;

import com.example.product_service.entity.Reviews;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewsQueryRepository {

    Page<Reviews> findAllByProductId(Long productId, Pageable pageable);
}
