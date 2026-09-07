package com.example.product_service.category.application.service;

import com.example.product_service.category.application.port.CategoryProductPort;
import com.example.product_service.category.application.port.CategoryRepository;
import com.example.product_service.category.application.service.dto.command.CreateCategoryCommand;
import com.example.product_service.category.domain.Category;
import com.example.product_service.category.exception.CategoryErrorCode;
import com.example.product_service.category.fixture.CategoryFixtureBuilder;
import com.example.product_service.common.exception.BusinessException;
import com.example.product_service.common.util.IdGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.example.product_service.category.fixture.CategoryCommandFixture.anCreateCategoryCommand;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class CategoryCommandServiceTest {

    @InjectMocks
    private CategoryCommandService categoryCommandService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryProductPort categoryProductPort;

    @Mock
    private IdGenerator idGenerator;

    @Captor
    private ArgumentCaptor<Category> categoryCaptor;

    @Test
    @DisplayName("최상위 카테고리를 생성한다")
    void createCategory() {
        //given
        Long expectedId = 100L;
        CreateCategoryCommand command = anCreateCategoryCommand().build();

        given(idGenerator.generate()).willReturn(expectedId);
        given(categoryRepository.save(any(Category.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        //when
        Long categoryId = categoryCommandService.createCategory(command);
        //then
        then(categoryRepository).should().save(categoryCaptor.capture());
        Category savedCategory = categoryCaptor.getValue();

        assertThat(categoryId).isEqualTo(expectedId);
        assertThat(savedCategory.getId()).isEqualTo(expectedId);
        assertThat(savedCategory.getDepth()).isEqualTo(1);
    }

    @Test
    @DisplayName("부모 아이디가 있으면 하위 카테고리를 생성한다")
    void createCategory_whenParentIdProvided_thenCreatesChildCategory() {
        //given
        Category parent = CategoryFixtureBuilder.given().build();
        Long expectedId = 200L;
        CreateCategoryCommand command = anCreateCategoryCommand()
                .parentId(parent.getId())
                .name("채소")
                .build();

        given(categoryRepository.findById(parent.getId())).willReturn(Optional.of(parent));
        given(categoryRepository.existsByParentIdAndName(parent.getId(), command.name())).willReturn(false);
        given(categoryProductPort.existsProductForCategory(parent.getId())).willReturn(false);
        given(idGenerator.generate()).willReturn(expectedId);
        given(categoryRepository.save(any(Category.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        //when
        Long categoryId = categoryCommandService.createCategory(command);
        //then
        then(categoryRepository).should().save(categoryCaptor.capture());
        Category savedCategory = categoryCaptor.getValue();

        assertThat(categoryId).isEqualTo(expectedId);
        assertThat(savedCategory.getId()).isEqualTo(expectedId);
        assertThat(savedCategory.getName()).isEqualTo(command.name());
        assertThat(savedCategory.getDepth()).isEqualTo(parent.getDepth() + 1);
        assertThat(savedCategory.getPath()).isEqualTo(parent.getPath() + "/" + expectedId);
        assertThat(savedCategory.getParent()).isEqualTo(parent);
    }

    @Test
    @DisplayName("부모 카테고리를 찾을 수 없으면 예외가 발생하고 카테고리를 생성하지 않는다")
    void createCategory_whenParentNotFound_thenThrownException() {
        //given
        Long parentId = 999L;
        CreateCategoryCommand command = anCreateCategoryCommand().parentId(parentId).build();

        given(categoryRepository.findById(parentId)).willReturn(Optional.empty());
        //when
        //then
        assertThatThrownBy(() -> categoryCommandService.createCategory(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CategoryErrorCode.CATEGORY_NOT_FOUND);

        then(categoryRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("같은 계층에 동일한 이름의 카테고리가 존재하면 예외가 발생하고 카테고리를 생성하지 않는다")
    void createCategory_whenSiblingNameDuplicated_thenThrownException() {
        //given
        Category parent = CategoryFixtureBuilder.given().build();
        CreateCategoryCommand command = anCreateCategoryCommand()
                .parentId(parent.getId())
                .name("채소")
                .build();

        given(categoryRepository.findById(parent.getId())).willReturn(Optional.of(parent));
        given(categoryRepository.existsByParentIdAndName(parent.getId(), command.name())).willReturn(true);
        //when
        //then
        assertThatThrownBy(() -> categoryCommandService.createCategory(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CategoryErrorCode.DUPLICATE_SIBLING_CATEGORY_NAME);

        then(categoryRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("부모 카테고리에 속한 상품이 존재하면 예외가 발생하고 카테고리를 생성하지 않는다")
    void createCategory_whenParentHasProduct_thenThrownException() {
        //given
        Category parent = CategoryFixtureBuilder.given().build();
        CreateCategoryCommand command = anCreateCategoryCommand()
                .parentId(parent.getId())
                .name("채소")
                .build();

        given(categoryRepository.findById(parent.getId())).willReturn(Optional.of(parent));
        given(categoryRepository.existsByParentIdAndName(parent.getId(), command.name())).willReturn(false);
        given(categoryProductPort.existsProductForCategory(parent.getId())).willReturn(true);
        //when
        //then
        assertThatThrownBy(() -> categoryCommandService.createCategory(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CategoryErrorCode.PARENT_CATEGORY_HAS_PRODUCT);

        then(categoryRepository).should(never()).save(any());
    }
}
