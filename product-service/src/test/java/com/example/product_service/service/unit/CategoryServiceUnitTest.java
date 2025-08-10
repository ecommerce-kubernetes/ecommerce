package com.example.product_service.service.unit;

import com.example.product_service.entity.Categories;
import com.example.product_service.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceUnitTest {

    @Mock
    CategoryRepository categoryRepository;

    @Test
    @DisplayName("카테고리 등록 테스트-성공")
    void saveCategoryTest_success(){

    }
}
