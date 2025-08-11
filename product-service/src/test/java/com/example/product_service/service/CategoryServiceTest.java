package com.example.product_service.service;

import com.example.product_service.dto.request.category.CategoryRequest;
import com.example.product_service.dto.request.category.UpdateCategoryRequest;
import com.example.product_service.dto.response.category.CategoryResponse;
import com.example.product_service.entity.Categories;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.exception.DuplicateResourceException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.CategoryRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

import static com.example.product_service.controller.util.MessagePath.*;
import static com.example.product_service.controller.util.TestMessageUtil.getMessage;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.verify;

@SpringBootTest
class CategoryServiceTest {

    @Autowired
    CategoryService categoryService;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    EntityManager em;

    private Categories parent;
    private Categories target;
    private Categories duplicate;

    @BeforeEach
    void saveFixture(){
        duplicate = categoryRepository.save(new Categories("duplicate", "http://test.jpg"));
        target = categoryRepository.save(new Categories("target", "http://target.jpg"));
        parent = categoryRepository.save(new Categories("parent", "http://test.jpg"));
    }

    @AfterEach
    void clearDB(){
        categoryRepository.deleteAll();
    }

    @Test
    @DisplayName("카테고리 생성 테스트-성공(상위 카테고리 생성시)")
    @Transactional
    void saveCategoryTest_integration_success_root(){
        CategoryRequest request = new CategoryRequest("name", null, "http://test.jpg");
        CategoryResponse response = categoryService.saveCategory(request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getName()).isEqualTo("name");
        assertThat(response.getParentId()).isNull();
        assertThat(response.getIconUrl()).isEqualTo("http://test.jpg");
    }

    @Test
    @DisplayName("카테고리 생성 테스트-성공(하위 카테고리 생성시)")
    @Transactional
    void saveCategoryTest_integration_success_leaf(){
        CategoryRequest request = new CategoryRequest("child", parent.getId(), null);
        CategoryResponse response = categoryService.saveCategory(request);

        assertThat(response.getName()).isEqualTo("child");
        assertThat(response.getParentId()).isEqualTo(parent.getId());
        assertThat(response.getIconUrl()).isEqualTo(null);
    }

    @Test
    @DisplayName("카테고리 생성 테스트-실패(부모 카테고리를 찾을 수 없음)")
    @Transactional
    void saveCategoryTest_integration_notFound(){
        CategoryRequest request = new CategoryRequest("name", 999L , null);
        assertThatThrownBy(()-> categoryService.saveCategory(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(CATEGORY_NOT_FOUND));
    }

    @Test
    @DisplayName("카테고리 생성 테스트-실패(카테고리 이름이 중복)")
    @Transactional
    void saveCategoryTest_integration_duplicate(){
        CategoryRequest request = new CategoryRequest("duplicate", null, null);
        assertThatThrownBy(() -> categoryService.saveCategory(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage(getMessage(CATEGORY_CONFLICT));
    }

    @Test
    @DisplayName("루트 카테고리 조회 테스트-성공")
    @Transactional
    void getRootCategoriesTest_integration_success(){
        List<CategoryResponse> response = categoryService.getRootCategories();

        assertThat(response)
                .extracting("id", "name", "parentId" ,"iconUrl")
                .containsExactlyInAnyOrder(
                        tuple(duplicate.getId(), duplicate.getName(), duplicate.getParent(), duplicate.getIconUrl()),
                        tuple(target.getId(), target.getName(), target.getParent(), target.getIconUrl()),
                        tuple(parent.getId(), parent.getName(), parent.getParent(), parent.getIconUrl())
                );
    }

    @Test
    @DisplayName("자식 카테고리 조회 테스트-성공")
    @Transactional
    void getChildrenCategoriesByIdTest_integration_success(){

        Categories child1 = categoryRepository.save(new Categories("child1", "http://child1.jpg"));
        Categories child2 = categoryRepository.save(new Categories("child2", "http://child2.jpg"));

        parent.addChild(child1);
        parent.addChild(child2);
        em.flush(); em.clear();

        List<CategoryResponse> response = categoryService.getChildrenCategoriesById(parent.getId());

        assertThat(response)
                .extracting("id", "name", "parentId", "iconUrl")
                .containsExactlyInAnyOrder(
                        tuple(child1.getId(), child1.getName(), child1.getParent().getId(), child1.getIconUrl()),
                        tuple(child2.getId(), child2.getName(), child2.getParent().getId(), child2.getIconUrl())
                );
    }

    @Test
    @DisplayName("자식 카테고리 조회 테스트-실패(카테고리를 찾을 수 없음)")
    void getChildrenCategoriesByIdTest_integration_notFound(){
        assertThatThrownBy(() -> categoryService.getChildrenCategoriesById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(CATEGORY_NOT_FOUND));
    }

    @Test
    @DisplayName("카테고리 수정 테스트-성공")
    @Transactional
    void updateCategoryByIdTest_integration_success(){
        UpdateCategoryRequest request = new UpdateCategoryRequest("updated", parent.getId(), "http://updated.jpg");

        CategoryResponse response = categoryService.updateCategoryById(target.getId(), request);

        assertThat(response.getId()).isEqualTo(target.getId());
        assertThat(response.getName()).isEqualTo("updated");
        assertThat(response.getParentId()).isEqualTo(parent.getId());
        assertThat(response.getIconUrl()).isEqualTo("http://updated.jpg");

        assertThat(target.getName()).isEqualTo("updated");
        assertThat(target.getParent().getId()).isEqualTo(parent.getId());
        assertThat(target.getIconUrl()).isEqualTo("http://updated.jpg");
    }

    @Test
    @DisplayName("카테고리 수정 테스트-실패(타깃 카테고리를 찾을 수 없음)")
    void updateCategoryTest_integration_notFound_target(){
        assertThatThrownBy(() -> categoryService.updateCategoryById(999L,
                new UpdateCategoryRequest("updated", null, null)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(CATEGORY_NOT_FOUND));
    }

    @Test
    @DisplayName("카테고리 수정 테스트-실패(부모 카테고리를 찾을 수 없을)")
    void updateCategoryTest_integration_notFound_parent(){
        assertThatThrownBy(()-> categoryService.updateCategoryById(target.getId(),
                new UpdateCategoryRequest("updated", 999L, null)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(CATEGORY_NOT_FOUND));
    }

    @Test
    @DisplayName("카테고리 수정 테스트-실패(중복 이름)")
    void updateCategoryTest_integration_conflict_name(){
        assertThatThrownBy(() -> categoryService.updateCategoryById(target.getId(),
                new UpdateCategoryRequest("duplicate", parent.getId(), "http://updated.jpg")))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage(getMessage(CATEGORY_CONFLICT));
    }

    @Test
    @DisplayName("카테고리 수정 테스트-실패(부모 카테고리 Id가 타깃의 Id 인경우")
    void updateCategoryTest_integration_badRequest_parentId(){
        assertThatThrownBy(() -> categoryService.updateCategoryById(target.getId(),
                new UpdateCategoryRequest("updated", target.getId(), "http://test.jpg")))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(CATEGORY_BAD_REQUEST));
    }

    @Test
    @DisplayName("카테고리 삭제 테스트-성공")
    @Transactional
    void deleteCategoryByIdTest_integration_success(){
        categoryService.deleteCategoryById(target.getId());
        em.flush(); em.clear();

        Optional<Categories> then = categoryRepository.findById(target.getId());
        assertThat(then).isEmpty();
    }

    @Test
    @DisplayName("카테고리 삭제 테스트-실패(타깃 카테고리가 없음)")
    void deleteCategoryTest_integration_notFound_target(){
        assertThatThrownBy(() -> categoryService.deleteCategoryById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(CATEGORY_NOT_FOUND));
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