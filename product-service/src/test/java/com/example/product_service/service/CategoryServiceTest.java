package com.example.product_service.service;

import com.example.product_service.dto.request.category.CategoryRequest;
import com.example.product_service.dto.request.category.UpdateCategoryRequest;
import com.example.product_service.dto.response.category.CategoryHierarchyResponse;
import com.example.product_service.dto.response.category.CategoryResponse;
import com.example.product_service.entity.Category;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.example.product_service.common.MessagePath.*;
import static com.example.product_service.util.TestMessageUtil.getMessage;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class CategoryServiceTest {

    @Autowired
    CategoryService categoryService;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    EntityManager em;

    private Category parent;
    private Category target;
    private Category duplicate;

    @BeforeEach
    void saveFixture(){
        duplicate = categoryRepository.save(new Category("duplicate", "http://test.jpg"));
        target = categoryRepository.save(new Category("target", "http://target.jpg"));
        parent = categoryRepository.save(new Category("parent", "http://test.jpg"));
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

        Category child1 = categoryRepository.save(new Category("child1", "http://child1.jpg"));
        Category child2 = categoryRepository.save(new Category("child2", "http://child2.jpg"));

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
    @DisplayName("카테고리 계층 구조 조회 테스트-성공")
    @Transactional
    void getHierarchyByCategoryIdTest_integration_success(){
        Category level1_1 = new Category("level1_1", "http://level1-1.jpg");
        Category level2_1 = new Category("level2-1", "http://level2-1.jpg");
        Category level2_2 = new Category("level2_2", "http://level2_2.jpg");
        Category level3_1 = new Category("level3_1", "http://level3_1.jpg");
        Category level3_2 = new Category("level3_2", "http://level3_2.jpg");

        level1_1.addChild(level2_1);
        level1_1.addChild(level2_2);
        level2_1.addChild(level3_1);
        level2_1.addChild(level3_2);
        List<Category> categories = List.of(level1_1, level2_1, level2_2, level3_1, level3_2);
        categoryRepository.saveAll(categories);
        em.flush(); em.clear();

        CategoryHierarchyResponse response = categoryService.getHierarchyByCategoryId(level3_1.getId());

        assertThat(response.getAncestors())
                .extracting("name", "parentId", "iconUrl")
                .containsExactlyInAnyOrder(
                        tuple(level1_1.getName(), null, level1_1.getIconUrl()),
                        tuple(level2_1.getName(), level1_1.getId(), level2_1.getIconUrl()),
                        tuple(level3_1.getName(), level2_1.getId(), level3_1.getIconUrl()));

        assertThat(itemsByLevel(response.getSiblingsByLevel(), 1))
                .extracting(CategoryResponse::getId, CategoryResponse::getName, CategoryResponse::getParentId, CategoryResponse::getIconUrl)
                .containsExactlyInAnyOrder(
                        tuple(level1_1.getId(), level1_1.getName(), level1_1.getParent(), level1_1.getIconUrl()),
                        tuple(parent.getId(), parent.getName(), parent.getParent(), parent.getIconUrl()),
                        tuple(target.getId(), target.getName(), target.getParent(), target.getIconUrl()),
                        tuple(duplicate.getId(), duplicate.getName(), duplicate.getParent(), duplicate.getIconUrl()));

        assertThat(itemsByLevel(response.getSiblingsByLevel(), 2))
                .extracting(CategoryResponse::getId, CategoryResponse::getName, CategoryResponse::getParentId, CategoryResponse::getIconUrl)
                .containsExactlyInAnyOrder(
                        tuple(level2_1.getId(), level2_1.getName(), level2_1.getParent().getId(), level2_1.getIconUrl()),
                        tuple(level2_2.getId(), level2_2.getName(), level2_2.getParent().getId(), level2_2.getIconUrl()));

        assertThat(itemsByLevel(response.getSiblingsByLevel(), 3))
                .extracting(CategoryResponse::getId, CategoryResponse::getName, CategoryResponse::getParentId, CategoryResponse::getIconUrl)
                .containsExactlyInAnyOrder(
                        tuple(level3_1.getId(), level3_1.getName(), level3_1.getParent().getId(), level3_1.getIconUrl()),
                        tuple(level3_2.getId(), level3_2.getName(), level3_2.getParent().getId(), level3_2.getIconUrl()));

    }

    @Test
    @DisplayName("카테고리 계층 구조 조회 테스트-실패(타깃 카테고리가 없는경우)")
    void getHierarchyByCategoryIdTest_integration_notFound(){
        assertThatThrownBy(() -> categoryService.getHierarchyByCategoryId(999L))
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

        Optional<Category> then = categoryRepository.findById(target.getId());
        assertThat(then).isEmpty();
    }

    @Test
    @DisplayName("카테고리 삭제 테스트-실패(타깃 카테고리가 없음)")
    void deleteCategoryTest_integration_notFound_target(){
        assertThatThrownBy(() -> categoryService.deleteCategoryById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(CATEGORY_NOT_FOUND));
    }

    private List<CategoryResponse> itemsByLevel(List<CategoryHierarchyResponse.LevelItem> siblings, int level){
        return siblings.stream()
                .filter(s -> s.getLevel() == level)
                .findFirst()
                .map(CategoryHierarchyResponse.LevelItem::getItems)
                .orElseThrow(() -> new AssertionError("level " + level + "not Found"));
    }
}