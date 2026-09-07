package com.example.product_service.category.application.service;

import com.example.product_service.category.application.service.dto.command.CreateCategoryCommand;
import com.example.product_service.category.application.service.dto.command.UpdateCategoryCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryCommandService {

    public Long saveCategory(CreateCategoryCommand command) {
        return null;
    }

    public Long updateCategory(UpdateCategoryCommand command) {
        return null;
    }

    public Long moveParent(Long categoryId, Long parentId) {
        return null;
    }

    public void deleteCategory(Long categoryId) {

    }
}
