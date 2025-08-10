package com.example.product_service.service.unit;

import com.example.product_service.dto.request.category.CategoryRequest;
import com.example.product_service.dto.response.category.CategoryResponse;
import com.example.product_service.repository.CategoryRepository;
import com.example.product_service.service.CategoryService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceUnitTest {

    @Mock
    CategoryRepository categoryRepository;

    @InjectMocks
    CategoryService categoryService;

    @Test
    @DisplayName("카테고리 등록 테스트-성공(상위 카테고리 생성시)")
    void saveCategoryTest_success(){
        CategoryRequest request = new CategoryRequest("name", null, "http://test.jpg");
        CategoryResponse response = categoryService.saveCategory(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("name");
        assertThat(response.getIconUrl()).isEqualTo("http://test.jpg");
        assertThat(response.getParentId()).isNull();
    }
}
