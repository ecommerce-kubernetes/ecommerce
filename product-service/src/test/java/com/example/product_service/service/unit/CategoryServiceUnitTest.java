package com.example.product_service.service.unit;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.request.category.CategoryRequest;
import com.example.product_service.dto.request.category.UpdateCategoryRequest;
import com.example.product_service.dto.response.category.CategoryResponse;
import com.example.product_service.entity.Categories;
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

import javax.swing.text.html.Option;
import java.util.Objects;
import java.util.Optional;

import static com.example.product_service.controller.util.MessagePath.CATEGORY_CONFLICT;
import static com.example.product_service.controller.util.MessagePath.CATEGORY_NOT_FOUND;
import static com.example.product_service.controller.util.TestMessageUtil.getMessage;
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
    private ArgumentCaptor<Categories> captor;

    @InjectMocks
    CategoryService categoryService;

    @Test
    @DisplayName("카테고리 등록 테스트-성공(상위 카테고리 생성시)")
    void saveCategoryTest_unit_success_root(){
        CategoryRequest request = new CategoryRequest("name", null, "http://test.jpg");
        mockExistsName("name", false);
        when(categoryRepository.save(any(Categories.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CategoryResponse response = categoryService.saveCategory(request);

        verify(categoryRepository).save(captor.capture());

        Categories value = captor.getValue();
        assertThat(value.getParent()).isEqualTo(null);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("name");
        assertThat(response.getIconUrl()).isEqualTo("http://test.jpg");
        assertThat(response.getParentId()).isNull();
    }

    @Test
    @DisplayName("카테고리 등록 테스트-성공(하위 카테고리 생성시)")
    void saveCategoryTest_unit_success_leaf(){
        CategoryRequest request = new CategoryRequest("name", 1L, "http://test.jpg");
        Categories parent = createCategoriesWithSetId(1L, "parent", "http://test.jpg");

        mockExistsName("name", false);
        mockFindById(1L, parent);
        when(categoryRepository.save(any(Categories.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CategoryResponse response = categoryService.saveCategory(request);

        verify(categoryRepository).save(captor.capture());
        Categories value = captor.getValue();

        assertThat(value.getParent()).isNotNull();
        assertThat(value.getParent().getId()).isEqualTo(request.getParentId());

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("name");
        assertThat(response.getIconUrl()).isEqualTo("http://test.jpg");
        assertThat(response.getParentId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("카테고리 등록 테스트-실패(부모 카테고리를 찾을 수 없는 경우)")
    void saveCategoryTest_unit_notFound(){
        CategoryRequest request = new CategoryRequest("name", 1L, "http://test.jpg");
        mockExistsName("name", false);
        when(ms.getMessage(CATEGORY_NOT_FOUND)).thenReturn("Category not found");
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
        when(ms.getMessage(CATEGORY_CONFLICT)).thenReturn("Category already exists");

        assertThatThrownBy(() -> categoryService.saveCategory(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage(getMessage(CATEGORY_CONFLICT));
    }

    @Test
    @DisplayName("카테고리 수정 테스트-성공")
    void updateCategoryTest_unit_success(){
        UpdateCategoryRequest request = new UpdateCategoryRequest("updated", 1L, null);
        Categories parent = spy(createCategoriesWithSetId(1L, "parent", "http://test.jpg"));
        Categories target = spy(new Categories("name", "http://before.jpg"));

        mockFindById(1L, parent);
        mockFindById(2L, target);

        CategoryResponse response = categoryService.updateCategoryById(2L, request);

        assertThat(response.getName()).isEqualTo("updated");
        assertThat(response.getIconUrl()).isEqualTo("http://before.jpg");
        assertThat(response.getParentId()).isEqualTo(1L);

        verify(categoryRepository).findById(1L);
        verify(categoryRepository).findById(2L);
        verify(target).modifyParent(parent);
    }

    private Categories createCategoriesWithSetId(Long id, String name, String url){
        Categories categories = new Categories(name, url);
        ReflectionTestUtils.setField(categories, "id", id);
        return categories;
    }

    private void mockExistsName(String name, boolean isExists){
        OngoingStubbing<Boolean> when = when(categoryRepository.existsByName(name));
        if(isExists){
            when.thenReturn(true);
        } else {
            when.thenReturn(false);
        }
    }

    private void mockFindById(Long id, Categories o){
        OngoingStubbing<Optional<Categories>> when = when(categoryRepository.findById(id));
        if(o == null){
            when.thenReturn(Optional.empty());
        } else {
            when .thenReturn(Optional.of(o));
        }
    }
}
