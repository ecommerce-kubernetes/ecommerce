package com.example.product_service.api.category.domain.repository.query;

public interface CategoryQueryDslRepository {
    boolean existsDuplicateName(Long parentId, String name);
}
