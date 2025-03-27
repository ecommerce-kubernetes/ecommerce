package com.example.product_service.service;

import com.example.product_service.dto.request.CategoryRequestDto;
import com.example.product_service.dto.response.CategoryResponseDto;
import com.example.product_service.entity.Categories;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.CategoriesRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class CategoryServiceImplTest {

    @Autowired
    CategoryService categoryService;

    @Autowired
    CategoriesRepository categoriesRepository;

    @Test
    @DisplayName("카테고리 생성 테스트")
    @Transactional
    void saveCategoryTest(){
        CategoryRequestDto categoryRequestDto = new CategoryRequestDto("식품");
        CategoryResponseDto categoryResponseDto = categoryService.saveCategory(categoryRequestDto);

        assertThat(categoryResponseDto.getName()).isEqualTo(categoryRequestDto.getName());
    }

    @Test
    @DisplayName("카테고리 수정 테스트")
    @Transactional
    void modifyCategoryTest(){
        Categories food = categoriesRepository.save(new Categories("식품"));

        CategoryResponseDto categoryResponseDto =
                categoryService.modifyCategory(food.getId(), new CategoryRequestDto("전자기기"));

        assertThat(categoryResponseDto.getName()).isEqualTo("전자기기");
        Categories categories = categoriesRepository.findById(food.getId()).orElseThrow();
        assertThat(categories.getName()).isEqualTo("전자기기");
    }

    @Test
    @DisplayName("카테고리 수정 테스트 - 카테고리를 찾을 수 없을때")
    void modifyCategoryTest_NotFoundCategory(){
        assertThatThrownBy(() -> categoryService.modifyCategory(999L, new CategoryRequestDto("전자기기")))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Not Found Category");
    }

    @Test
    @DisplayName("카테고리 삭제 테스트")
    void deleteCategoryTest(){
        Categories food = categoriesRepository.save(new Categories("식품"));

        categoryService.deleteCategory(food.getId());

        Optional<Categories> category = categoriesRepository.findById(food.getId());

        assertThat(category).isEmpty();
    }

    @Test
    @DisplayName("카테고리 삭제 테스트 - 없는 카테고리 삭제시")
    void deleteCategoryTest_NotFoundCategory(){
        assertThatThrownBy(() -> categoryService.deleteCategory(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Not Found Category");
    }
}