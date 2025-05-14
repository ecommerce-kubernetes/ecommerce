package com.example.product_service.service;

import com.example.product_service.dto.request.CategoryRequestDto;
import com.example.product_service.dto.response.CategoryResponseDto;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.entity.Categories;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.CategoriesRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Slf4j
class CategoryServiceImplTest {

    @Autowired
    CategoryService categoryService;

    @Autowired
    CategoriesRepository categoriesRepository;

    @AfterEach
    void clearDB(){
        categoriesRepository.deleteAll();
    }

    @Test
    @DisplayName("대표 카테고리 생성 테스트")
    @Transactional
    void saveCategoryTest_Main(){
        //대표 카테고리 생성 - parentId == null
        CategoryRequestDto categoryRequestDto = new CategoryRequestDto("식품", null);
        CategoryResponseDto categoryResponseDto = categoryService.saveCategory(categoryRequestDto);

        assertThat(categoryResponseDto.getName()).isEqualTo(categoryRequestDto.getName());
        assertThat(categoryResponseDto.getChildren()).isEmpty();
    }

    @Test
    @DisplayName("서브 카테고리 생성 테스트")
    @Transactional
    void saveCategoryTest_Sub(){
        //부모 카테고리
        Categories parent = categoriesRepository.save(new Categories("식품"));

        //자식 카테고리 생성 - parentId == 부모 카테고리 ID
        CategoryRequestDto sideDishRequest = new CategoryRequestDto("반찬류", parent.getId());
        CategoryResponseDto sideDishResponse = categoryService.saveCategory(sideDishRequest);

        assertThat(sideDishResponse.getName()).isEqualTo(sideDishRequest.getName());
        assertThat(sideDishResponse.getChildren()).isEmpty();

        assertThat(parent.getChildren().size()).isEqualTo(1);

        Categories subCategory = categoriesRepository.findById(sideDishResponse.getId()).orElseThrow();
        assertThat(parent.getChildren()).contains(subCategory);
    }

    @Test
    @DisplayName("서브 카테고리 생성 테스트_부모 카테고리를 찾을 수 없음")
    @Transactional
    void saveCategoryTest_Sub_NotFoundParentCategory(){
        CategoryRequestDto sideDishRequest = new CategoryRequestDto("반찬류", 999L);
        assertThatThrownBy(()-> categoryService.saveCategory(sideDishRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Not Found Parent Category");
    }

    @Test
    @DisplayName("카테고리 수정 테스트")
    @Transactional
    void modifyCategoryTest_EditName(){
        Categories food = categoriesRepository.save(new Categories("식품"));
        Categories electronicDevice = categoriesRepository.save(new Categories("전자기기"));
        Categories modifyCategory = categoriesRepository.save(new Categories("반찬류"));
        food.addChild(modifyCategory);

        CategoryResponseDto categoryResponseDto =
                categoryService.modifyCategory(modifyCategory.getId(), new CategoryRequestDto("노트북", electronicDevice.getId()));

        assertThat(categoryResponseDto.getName()).isEqualTo("노트북");
        assertThat(food.getChildren().size()).isEqualTo(0);
        assertThat(food.getChildren()).doesNotContain(modifyCategory);
        assertThat(electronicDevice.getChildren().size()).isEqualTo(1);
        assertThat(electronicDevice.getChildren()).contains(modifyCategory);
    }

    @Test
    @DisplayName("카테고리 수정 테스트 - 카테고리를 찾을 수 없을때")
    void modifyCategoryTest_NotFoundCategory(){
        assertThatThrownBy(() -> categoryService.modifyCategory(999L, new CategoryRequestDto("전자기기", null)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Not Found Category");
    }

    @Test
    @DisplayName("카테고리 수정 테스트 - 부모 카테고리를 찾을 수 없을때")
    void modifyCategoryTest_NotFoundParentCategory(){
        Categories modifyCategory = categoriesRepository.save(new Categories("반찬류"));
        assertThatThrownBy(()-> categoryService.modifyCategory(modifyCategory.getId(), new CategoryRequestDto("노트북", 999L)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Not Found Parent Category");
    }

    @Test
    @DisplayName("카테고리 삭제 테스트")
    @Transactional
    void deleteCategoryTest(){
        Categories food = categoriesRepository.save(new Categories("식품"));
        Categories deleteCategory = categoriesRepository.save(new Categories("반찬류"));

        food.addChild(deleteCategory);

        categoryService.deleteCategory(deleteCategory.getId());

        Optional<Categories> category = categoriesRepository.findById(deleteCategory.getId());

        assertThat(category).isEmpty();

        assertThat(food.getChildren()).doesNotContain(deleteCategory);
    }

    @Test
    @DisplayName("카테고리 삭제 테스트 - 없는 카테고리 삭제시")
    void deleteCategoryTest_NotFoundCategory(){
        assertThatThrownBy(() -> categoryService.deleteCategory(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Not Found Category");
    }

//    @Test
//    @DisplayName("카테고리 정보 조회")
//    void getCategoryDetailsTest(){
//        Categories save = categoriesRepository.save(new Categories("식품"));
//
//        CategoryResponseDto categoryDetails = categoryService.getCategoryDetails(save.getId());
//
//        assertThat(categoryDetails.getId()).isEqualTo(save.getId());
//        assertThat(categoryDetails.getName()).isEqualTo(save.getName());
//    }

//    @Test
//    @DisplayName("카테고리 정보 조회 - 없는 카테고리 일때")
//    void getCategoryDetailsTest_NotFoundCategory(){
//        assertThatThrownBy(() -> categoryService.getCategoryDetails(999L))
//                .isInstanceOf(NotFoundException.class)
//                .hasMessage("Not Found Category");
//    }
//
//    @Test
//    @DisplayName("카테고리 리스트 조회")
//    void getCategoryListTest(){
//        List<Categories> categories = new ArrayList<>();
//        categories.add(new Categories("식품"));
//        categories.add(new Categories("전자기기"));
//        categories.add(new Categories("의류"));
//        categories.add(new Categories("가구"));
//
//        categoriesRepository.saveAll(categories);
//
//        Pageable pageable = PageRequest.of(0, 10, Sort.Direction.ASC, "id");
//
//        PageDto<CategoryResponseDto> result = categoryService.getCategoryList(pageable);
//
//        assertThat(result.getCurrentPage()).isEqualTo(0);
//        assertThat(result.getPageSize()).isEqualTo(10);
//        assertThat(result.getTotalPage()).isEqualTo(1);
//        assertThat(result.getTotalElement()).isEqualTo(4);
//
//        List<CategoryResponseDto> content = result.getContent();
//        for (int i = 0; i < content.size(); i++) {
//            assertThat(content.get(i).getName()).isEqualTo(categories.get(i).getName());
//        }
//    }

}