package com.example.product_service.service;

import com.example.product_service.dto.request.category.CategoryRequest;
import com.example.product_service.dto.request.category.UpdateCategoryRequest;
import com.example.product_service.dto.response.category.CategoryResponse;
import com.example.product_service.entity.Categories;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.CategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Slf4j
class CategoryServiceUnitTest {

    @Autowired
    CategoryService categoryService;

    @Autowired
    CategoryRepository categoryRepository;

    @AfterEach
    void clearDB(){
        categoryRepository.deleteAll();
    }

    @Test
    @DisplayName("대표 카테고리 생성 테스트")
    @Transactional
    void saveCategoryTest_Main(){
        //대표 카테고리 생성 - parentId == null
        CategoryRequest categoryRequestDto = new CategoryRequest("식품", null, "http://test.jpg");
        CategoryResponse categoryResponseDto = categoryService.saveCategory(categoryRequestDto);

        assertThat(categoryResponseDto.getName()).isEqualTo(categoryRequestDto.getName());
        assertThat(categoryResponseDto.getParentId()).isEqualTo(null);
        assertThat(categoryResponseDto.getIconUrl()).isEqualTo(categoryRequestDto.getIconUrl());
    }

    @Test
    @DisplayName("서브 카테고리 생성 테스트")
    @Transactional
    void saveCategoryTest_Sub(){
        //부모 카테고리
        Categories parent = categoryRepository.save(new Categories("식품","http://test.jpg"));

        //자식 카테고리 생성 - parentId == 부모 카테고리 ID
        CategoryRequest sideDishRequest = new CategoryRequest("반찬류", parent.getId(), null);
        CategoryResponse sideDishResponse = categoryService.saveCategory(sideDishRequest);

        assertThat(sideDishResponse.getName()).isEqualTo(sideDishRequest.getName());
        assertThat(sideDishResponse.getParentId()).isEqualTo(parent.getId());
        assertThat(sideDishResponse.getIconUrl()).isEqualTo(sideDishRequest.getIconUrl());
        assertThat(parent.getChildren().size()).isEqualTo(1);

        Categories subCategory = categoryRepository.findById(sideDishResponse.getId()).orElseThrow();
        assertThat(parent.getChildren()).contains(subCategory);
    }

    @Test
    @DisplayName("서브 카테고리 생성 테스트_부모 카테고리를 찾을 수 없음")
    @Transactional
    void saveCategoryTest_Sub_NotFoundParentCategory(){
        CategoryRequest sideDishRequest = new CategoryRequest("반찬류", 999L , null);
        assertThatThrownBy(()-> categoryService.saveCategory(sideDishRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Not Found Parent Category");
    }

    @Test
    @DisplayName("카테고리 수정 테스트")
    @Transactional
    void updateCategoryByIdTest_EditName(){
        Categories food = categoryRepository.save(new Categories("식품", "http://test.jpg"));
        Categories electronicDevice = categoryRepository.save(new Categories("전자기기" , null));
        Categories modifyCategory = categoryRepository.save(new Categories("반찬류", null));
        food.addChild(modifyCategory);

        UpdateCategoryRequest modifyRequestDto = new UpdateCategoryRequest("노트북", electronicDevice.getId(), "http://test2.jpg");

        CategoryResponse categoryResponseDto =
                categoryService.updateCategoryById(modifyCategory.getId(), modifyRequestDto);

        assertThat(categoryResponseDto.getName()).isEqualTo(modifyRequestDto.getName());
        assertThat(categoryResponseDto.getParentId()).isEqualTo(electronicDevice.getId());
        assertThat(categoryResponseDto.getIconUrl()).isEqualTo(modifyRequestDto.getIconUrl());
        assertThat(food.getChildren().size()).isEqualTo(0);
        assertThat(food.getChildren()).doesNotContain(modifyCategory);
        assertThat(electronicDevice.getChildren().size()).isEqualTo(1);
        assertThat(electronicDevice.getChildren()).contains(modifyCategory);
    }

    @Test
    @DisplayName("카테고리 수정 테스트 - 카테고리를 찾을 수 없을때")
    void updateCategoryTest_NotFoundCategoryById(){
        assertThatThrownBy(() -> categoryService.updateCategoryById(999L, new UpdateCategoryRequest("전자기기", null, null)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Not Found Category");
    }

    @Test
    @DisplayName("카테고리 수정 테스트 - 부모 카테고리를 찾을 수 없을때")
    void updateCategoryTest_NotFoundParentCategoryById(){
        Categories modifyCategory = categoryRepository.save(new Categories("반찬류", null));
        assertThatThrownBy(()-> categoryService.updateCategoryById(modifyCategory.getId(), new UpdateCategoryRequest("노트북", 999L, null)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Not Found Parent Category");
    }

    @Test
    @DisplayName("카테고리 삭제 테스트")
    @Transactional
    void deleteCategoryByIdTest(){
        Categories food = categoryRepository.save(new Categories("식품", "http://localhost.jpg"));
        Categories deleteCategory = categoryRepository.save(new Categories("반찬류", null));

        food.addChild(deleteCategory);

        categoryService.deleteCategoryById(deleteCategory.getId());

        Optional<Categories> category = categoryRepository.findById(deleteCategory.getId());

        assertThat(category).isEmpty();

        assertThat(food.getChildren()).doesNotContain(deleteCategory);
    }

    @Test
    @DisplayName("카테고리 삭제 테스트 - 없는 카테고리 삭제시")
    void deleteCategoryTest_NotFoundCategoryById(){
        assertThatThrownBy(() -> categoryService.deleteCategoryById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Not Found Category");
    }

    @Test
    @DisplayName("단일 카테고리 정보 조회")
    @Transactional
    void getCategoryDetailsTest(){
        Categories parent = categoryRepository.save(new Categories("식품", "http://test.jpg"));
        Categories sub1 = categoryRepository.save(new Categories("반찬류", null));
        Categories sub2 = categoryRepository.save(new Categories("냉장", null));

        parent.addChild(sub1);
        parent.addChild(sub2);

        CategoryResponse categoryDetails = categoryService.getCategoryDetails(sub1.getId());

        assertThat(categoryDetails.getId()).isEqualTo(sub1.getId());
        assertThat(categoryDetails.getName()).isEqualTo(sub1.getName());
        assertThat(categoryDetails.getIconUrl()).isEqualTo(sub1.getIconUrl());
        assertThat(categoryDetails.getParentId()).isEqualTo(parent.getId());


    }

    @Test
    @DisplayName("카테고리 정보 조회 - 없는 카테고리 일때")
    void getCategoryDetailsTest_NotFoundCategory(){
        assertThatThrownBy(() -> categoryService.getCategoryDetails(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Not Found Category");
    }

    @Test
    @DisplayName("대표 카테고리 리스트 조회")
    void getCategoryListTest(){
        List<Categories> categories = new ArrayList<>();
        categories.add(new Categories("식품", null));
        categories.add(new Categories("전자기기", null));
        categories.add(new Categories("의류", null));
        categories.add(new Categories("가구", null));

        categoryRepository.saveAll(categories);

        Pageable pageable = PageRequest.of(0, 10, Sort.Direction.ASC, "id");

    }

    @Test
    @DisplayName("자식 카테고리 리스트 조회")
    @Transactional
    void getChildCategoriesTest(){
        Categories parent = categoryRepository.save(new Categories("식품", null));
        Categories sub1 = categoryRepository.save(new Categories("반찬류", null));
        Categories sub2 = categoryRepository.save(new Categories("냉동", null));
        Categories sub3 = categoryRepository.save(new Categories("냉장", null));

        parent.addChild(sub1);
        parent.addChild(sub2);
        parent.addChild(sub3);

        List<CategoryResponse> childCategories = categoryService.getChildrenCategoriesById(parent.getId());

        assertThat(childCategories.size()).isEqualTo(3);
    }

    @Test
    @DisplayName("특정 카테고리의 루트 카테고리 조회")
    @Transactional
    void getRootCategoryDetailsOfTest(){
        Categories parent = categoryRepository.save(new Categories("식품", null));
        Categories sub1 = categoryRepository.save(new Categories("반찬류", null));
        Categories sub2 = categoryRepository.save(new Categories("냉동", null));
        parent.addChild(sub1);
        parent.addChild(sub2);


        Categories sub1_sub1 = categoryRepository.save(new Categories("김", null));

        sub1.addChild(sub1_sub1);

        CategoryResponse rootCategoryResponse = categoryService.getRootCategoryDetailsOf(sub1_sub1.getId());

        assertThat(rootCategoryResponse.getId()).isEqualTo(parent.getId());
        assertThat(rootCategoryResponse.getName()).isEqualTo(parent.getName());
        assertThat(rootCategoryResponse.getParentId()).isEqualTo(null);
        assertThat(rootCategoryResponse.getIconUrl()).isEqualTo(parent.getIconUrl());
    }

    @Test
    @DisplayName("특정 카테고리의 루트 카테고리 조회_카테고리를 찾을 수 없을경우")
    @Transactional
    void getRootCategoryDetailsOfTest_NotFoundCategory(){
        assertThatThrownBy(() -> categoryService.getRootCategoryDetailsOf(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Not Found Category");
    }
}