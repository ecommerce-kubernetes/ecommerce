package com.example.product_service.category.domain.repository.query;

public interface CategoryQueryDslRepository {
    boolean existsDuplicateName(Long parentId, String name);
}
