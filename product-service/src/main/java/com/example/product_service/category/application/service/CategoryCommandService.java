package com.example.product_service.category.application.service;

import com.example.product_service.category.application.service.dto.command.CategoryCommand;
import com.example.product_service.category.domain.repository.CategoryRepository;
import com.example.product_service.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryCommandService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public Long saveCategory(CategoryCommand.Create command) {
        return null;
    }

    public Long updateCategory(CategoryCommand.Update command) {
        return null;
    }

    public Long moveParent(Long categoryId, Long parentId) {
        return null;
    }

    public void deleteCategory(Long categoryId) {

    }
}
