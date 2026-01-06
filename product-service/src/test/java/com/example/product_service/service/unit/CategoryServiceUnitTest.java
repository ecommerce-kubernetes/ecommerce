package com.example.product_service.service.unit;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.api.category.controller.dto.CategoryRequest;
import com.example.product_service.controller.UpdateCategoryRequest;
import com.example.product_service.dto.response.category.CategoryHierarchyResponse;
import com.example.product_service.api.category.service.dto.result.CategoryResponse;
import com.example.product_service.entity.Category;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.exception.DuplicateResourceException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.CategoryRepository;
import com.example.product_service.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static com.example.product_service.common.MessagePath.*;
import static com.example.product_service.util.TestMessageUtil.getMessage;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceUnitTest {

    @Mock
    CategoryRepository categoryRepository;
    @Mock
    MessageSourceUtil ms;
    @Captor
    private ArgumentCaptor<Category> captor;

    @InjectMocks
    CategoryService categoryService;

    @Test
    @DisplayName("카테고리 등록 테스트-성공(상위 카테고리 생성시)")
    void saveCategoryTest_unit_success_root(){
        CategoryRequest request = new CategoryRequest("name", null, "http://test.jpg");
        mockExistsName("name", false);
        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CategoryResponse response = categoryService.saveCategory(request);

        verify(categoryRepository).save(captor.capture());

        Category value = captor.getValue();
        assertThat(value.getParent()).isEqualTo(null);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("name");
        assertThat(response.getImageUrl()).isEqualTo("http://test.jpg");
        assertThat(response.getParentId()).isNull();
    }

    @Test
    @DisplayName("카테고리 등록 테스트-성공(하위 카테고리 생성시)")
    void saveCategoryTest_unit_success_leaf(){
        CategoryRequest request = new CategoryRequest("name", 1L, "http://test.jpg");
        Category parent = createCategoriesWithSetId(1L, "parent", "http://test.jpg");

        mockExistsName("name", false);
        mockFindById(1L, parent);
        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CategoryResponse response = categoryService.saveCategory(request);

        verify(categoryRepository).save(captor.capture());
        Category value = captor.getValue();

        assertThat(value.getParent()).isNotNull();
        assertThat(value.getParent().getId()).isEqualTo(request.getParentId());

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("name");
        assertThat(response.getImageUrl()).isEqualTo("http://test.jpg");
        assertThat(response.getParentId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("카테고리 등록 테스트-실패(부모 카테고리를 찾을 수 없는 경우)")
    void saveCategoryTest_unit_notFound(){
        CategoryRequest request = new CategoryRequest("name", 1L, "http://test.jpg");
        mockExistsName("name", false);
        mockMessageUtil(CATEGORY_NOT_FOUND, "Category not found");
        mockFindById(1L, null);

        assertThatThrownBy(() -> categoryService.saveCategory(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(CATEGORY_NOT_FOUND));
    }

    @Test
    @DisplayName("카테고리 등록 테스트-실패(중복 이름)")
    void saveCategoryTest_unit_Duplicate(){
        CategoryRequest request = new CategoryRequest("duplicate", 1L, "http://test.jpg");
        mockExistsName("duplicate", true);
        mockMessageUtil(CATEGORY_CONFLICT, "Category already exists");

        assertThatThrownBy(() -> categoryService.saveCategory(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage(getMessage(CATEGORY_CONFLICT));
    }

    @Test
    @DisplayName("루트 카테고리 조회 테스트-성공")
    void getRootCategoriesTest_unit_success(){
        List<Category> roots = List.of(createCategoriesWithSetId(1L, "root1", "http://root1.jpg"),
                createCategoriesWithSetId(2L, "root2", "http://root2.jpg"));
        when(categoryRepository.findByParentIsNull()).thenReturn(roots);

        List<CategoryResponse> response = categoryService.getRootCategories();

        assertThat(response).hasSize(2);
        assertThat(response)
                .extracting("id", "name", "parentId" ,"iconUrl")
                .containsExactlyInAnyOrder(
                        tuple(1L, "root1", null,"http://root1.jpg"),
                        tuple(2L, "root2", null, "http://root2.jpg")
        );
    }

    @Test
    @DisplayName("자식 카테고리 조회 테스트-성공")
    void getChildrenCategoriesByIdTest_unit_success(){
        Category parent = createCategoriesWithSetId(1L, "parent", "http://parent.jpg");
        addChildren(parent, new Category("child1", "http://child1.jpg"), new Category("child2", "http://child2.jpg"));
        mockFindById(1L, parent);

        List<CategoryResponse> response = categoryService.getChildrenCategoriesById(1L);

        assertThat(response)
                .extracting( "name", "parentId", "iconUrl")
                .containsExactlyInAnyOrder(
                        tuple("child1", 1L, "http://child1.jpg"),
                        tuple("child2", 1L, "http://child2.jpg")
                );
    }

    @Test
    @DisplayName("자식 카테고리 조회 테스트-실패(카테고리를 찾을 수 없는 경우)")
    void getChildrenCategoriesByIdTest_unit_notFound(){
        mockFindById(1L, null);
        mockMessageUtil(CATEGORY_NOT_FOUND, "Category not found");

        assertThatThrownBy(() -> categoryService.getChildrenCategoriesById(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(CATEGORY_NOT_FOUND));
    }

    @Test
    @DisplayName("카테고리 계층 구조 조회 테스트-성공")
    void getHierarchyByCategoryIdTest_unit_success(){
        Category level1_1 = createCategoriesWithSetId(1L, "level1-1", "http://level1-1.jpg");
        Category level1_2 = createCategoriesWithSetId(2L, "level1-2", "http://level1-2.jpg");

        Category level2_1 = createCategoriesWithSetId(3L, "level2-1", "http://level2-1.jpg");
        Category level2_2 = createCategoriesWithSetId(4L, "level2-2", "http://level2-2.jpg");

        Category level3_1 = createCategoriesWithSetId(5L, "level3-1", "http://level3-1.jpg");
        Category level3_2 = createCategoriesWithSetId(6L, "level3-2", "http://level3-2.jpg");

        addChildren(level1_1, level2_1, level2_2);
        addChildren(level2_1, level3_1, level3_2);

        //categoryId == level3_2.id
        mockFindWithParentById(6L, level3_2);
        when(categoryRepository.findByParentIsNull()).thenReturn(List.of(level1_1, level1_2));
        CategoryHierarchyResponse response = categoryService.getHierarchyByCategoryId(6L);

        assertThat(response.getAncestors())
                .extracting("name", "parentId", "iconUrl")
                .containsExactlyInAnyOrder(
                        tuple(level1_1.getName(), null, level1_1.getIconUrl()),
                        tuple(level2_1.getName(), level1_1.getId(), level2_1.getIconUrl()),
                        tuple(level3_2.getName(), level2_1.getId(), level3_2.getIconUrl()));

        assertThat(itemsByLevel(response.getSiblingsByLevel(), 1))
                .extracting(CategoryResponse::getId, CategoryResponse::getName, CategoryResponse::getParentId, CategoryResponse::getImageUrl)
                .containsExactlyInAnyOrder(
                        tuple(level1_1.getId(), level1_1.getName(), level1_1.getParent(), level1_1.getIconUrl()),
                        tuple(level1_2.getId(), level1_2.getName(), level1_2.getParent(), level1_2.getIconUrl()));

        assertThat(itemsByLevel(response.getSiblingsByLevel(), 2))
                .extracting(CategoryResponse::getId, CategoryResponse::getName, CategoryResponse::getParentId, CategoryResponse::getImageUrl)
                .containsExactlyInAnyOrder(
                        tuple(level2_1.getId(), level2_1.getName(), level2_1.getParent().getId(), level2_1.getIconUrl()),
                        tuple(level2_2.getId(), level2_2.getName(), level2_2.getParent().getId(), level2_2.getIconUrl()));

        assertThat(itemsByLevel(response.getSiblingsByLevel(), 3))
                .extracting(CategoryResponse::getId, CategoryResponse::getName, CategoryResponse::getParentId, CategoryResponse::getImageUrl)
                .containsExactlyInAnyOrder(
                        tuple(level3_1.getId(), level3_1.getName(), level3_1.getParent().getId(), level3_1.getIconUrl()),
                        tuple(level3_2.getId(), level3_2.getName(), level3_2.getParent().getId(), level3_2.getIconUrl()));
    }

    @Test
    @DisplayName("카테고리 계층 구조 조회 테스트-실패(카테고리를 찾을 수 없음)")
    void getHierarchyByCategoryIdTest_unit_notFound(){
        mockFindWithParentById(1L, null);
        when(ms.getMessage(CATEGORY_NOT_FOUND)).thenReturn("Category not found");
        assertThatThrownBy(() -> categoryService.getHierarchyByCategoryId(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(CATEGORY_NOT_FOUND));
    }

    @Test
    @DisplayName("카테고리 수정 테스트-성공(부모 변경)")
    void updateCategoryTest_unit_success_parent_notnull(){
        UpdateCategoryRequest request = new UpdateCategoryRequest("updated", 1L, null);
        Category parent = spy(createCategoriesWithSetId(1L, "parent", "http://test.jpg"));
        Category target = spy(new Category("name", "http://before.jpg"));

        mockFindById(1L, parent);
        mockFindById(2L, target);

        CategoryResponse response = categoryService.updateCategoryById(2L, request);

        assertThat(response.getName()).isEqualTo("updated");
        assertThat(response.getImageUrl()).isEqualTo("http://before.jpg");
        assertThat(response.getParentId()).isEqualTo(1L);

        verify(categoryRepository).findById(1L);
        verify(categoryRepository).findById(2L);

        verify(target).setName("updated");
        verify(target).modifyParent(parent);
        verify(target, never()).setIconUrl(any());
    }

    @Test
    @DisplayName("카테고리 수정 테스트-성공(부모 변경 x)")
    void updateCategoryTest_unit_success_parent_null(){
        UpdateCategoryRequest request = new UpdateCategoryRequest("updated", null, null);
        Category parent = spy(createCategoriesWithSetId(1L, "parent", "http://test.jpg"));
        Category target = spy(new Category("name", "http://before.jpg"));
        parent.addChild(target);

        mockFindById(2L, target);

        CategoryResponse response = categoryService.updateCategoryById(2L, request);

        assertThat(response.getName()).isEqualTo("updated");
        assertThat(response.getImageUrl()).isEqualTo("http://before.jpg");
        assertThat(response.getParentId()).isEqualTo(1L);

        verify(categoryRepository).findById(2L);

        verify(target).setName("updated");
        verify(target, never()).modifyParent(parent);
        verify(target, never()).setIconUrl(any());
    }

    @Test
    @DisplayName("카테고리 수정 테스트-실패(타깃 카테고리를 찾을 수 없음)")
    void updateCategoryTest_unit_notFound_target(){
        UpdateCategoryRequest request = new UpdateCategoryRequest("updated", 1L, null);
        mockFindById(2L, null);
        when(ms.getMessage(CATEGORY_NOT_FOUND)).thenReturn("Category not found");

        assertThatThrownBy(() -> categoryService.updateCategoryById(2L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(CATEGORY_NOT_FOUND));
    }

    @Test
    @DisplayName("카테고리 수정 테스트-실패(부모 카테고리 찾을 수 없음)")
    void updateCategoryTest_unit_notFound_parent(){
        UpdateCategoryRequest request = new UpdateCategoryRequest("update", 1L, null);
        Category target = new Category("name", "http://test.jpg");
        mockFindById(1L, null);
        mockFindById(2L, target);
        when(ms.getMessage(CATEGORY_NOT_FOUND)).thenReturn("Category not found");

        assertThatThrownBy(() -> categoryService.updateCategoryById(2L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(CATEGORY_NOT_FOUND));
    }

    @Test
    @DisplayName("카테고리 수정 테스트-실패(중복 이름)")
    void updateCategoryTest_unit_conflict_name(){
        UpdateCategoryRequest request = new UpdateCategoryRequest("duplicate", null, null);
        Category target = new Category("name", "http://test.jpg");
        mockFindById(2L, target);
        mockExistsName("duplicate", true);
        when(ms.getMessage(CATEGORY_CONFLICT)).thenReturn("Category already exists");

        assertThatThrownBy(() -> categoryService.updateCategoryById(2L, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage(getMessage(CATEGORY_CONFLICT));
    }

    @Test
    @DisplayName("카테고리 수정 테스트-실패(부모 카테고리 Id가 target의 Id인경우")
    void updateCategoryTest_unit_badRequest_parentId(){
        UpdateCategoryRequest request = new UpdateCategoryRequest("updated", 2L, null);
        Category target = createCategoriesWithSetId(2L, "name", "http://test.jpg");
        mockFindById(2L, target);
        mockExistsName("updated", false);
        when(ms.getMessage(CATEGORY_BAD_REQUEST)).thenReturn("Cannot assign an Category to itself as a parent.");

        assertThatThrownBy(() -> categoryService.updateCategoryById(2L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(CATEGORY_BAD_REQUEST));
    }

    @Test
    @DisplayName("카테고리 삭제 테스트-성공")
    void deleteCategoryTest_unit_success(){
        Category target = createCategoriesWithSetId(1L, "target", "http://test.jpg");
        mockFindById(1L, target);
        categoryService.deleteCategoryById(1L);
        verify(categoryRepository).delete(captor.capture());

        Category value = captor.getValue();
        assertThat(value.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("카테고리 삭제 테스트-실패(타깃 카테고리가 없음)")
    void deleteCategoryTest_unit_notFound_target(){
        mockFindById(1L, null);
        when(ms.getMessage(CATEGORY_NOT_FOUND)).thenReturn("Category not found");
        assertThatThrownBy(()-> categoryService.deleteCategoryById(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(CATEGORY_NOT_FOUND));
    }

    private Category createCategoriesWithSetId(Long id, String name, String url){
        Category category = new Category(name, url);
        ReflectionTestUtils.setField(category, "id", id);
        return category;
    }

    private void mockExistsName(String name, boolean isExists){
        OngoingStubbing<Boolean> when = when(categoryRepository.existsByName(name));
        if(isExists){
            when.thenReturn(true);
        } else {
            when.thenReturn(false);
        }
    }

    private void mockFindById(Long id, Category o){
        OngoingStubbing<Optional<Category>> when = when(categoryRepository.findById(id));
        if(o == null){
            when.thenReturn(Optional.empty());
        } else {
            when.thenReturn(Optional.of(o));
        }
    }

    private void mockFindWithParentById(Long id, Category o){
        OngoingStubbing<Optional<Category>> when = when(categoryRepository.findWithParentById(id));
        if(o == null){
            when.thenReturn(Optional.empty());
        } else {
            when.thenReturn(Optional.of(o));
        }
    }

    private void mockMessageUtil(String code, String returnMessage){
        when(ms.getMessage(code)).thenReturn(returnMessage);
    }

    private void addChildren(Category parent, Category... child){
        for (Category category : child) {
            parent.addChild(category);
        }
    }

    private List<CategoryResponse> itemsByLevel(List<CategoryHierarchyResponse.LevelItem> siblings, int level){
        return siblings.stream()
                .filter(s -> s.getLevel() == level)
                .findFirst()
                .map(CategoryHierarchyResponse.LevelItem::getItems)
                .orElseThrow(() -> new AssertionError("level " + level + "not Found"));
    }
}
