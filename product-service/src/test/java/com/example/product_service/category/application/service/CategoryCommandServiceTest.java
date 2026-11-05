package com.example.product_service.category.application.service;

import com.example.product_service.category.application.port.CategoryProductPort;
import com.example.product_service.category.application.port.CategoryRepository;
import com.example.product_service.category.application.service.dto.command.CreateCategoryCommand;
import com.example.product_service.category.application.service.dto.command.UpdateCategoryCommand;
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

import java.util.List;
import java.util.Optional;

import static com.example.product_service.category.fixture.CategoryCommandFixture.anCreateCategoryCommand;
import static com.example.product_service.category.fixture.CategoryCommandFixture.anUpdateCategoryCommand;
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

    @Test
    @DisplayName("이름 변경 없이 카테고리를 수정하면 중복 검증을 하지 않는다")
    void updateCategory_whenNameUnchanged_thenSkipsDuplicateCheck() {
        //given
        Category category = CategoryFixtureBuilder.given().withName("카테고리").build();
        UpdateCategoryCommand command = anUpdateCategoryCommand()
                .id(category.getId())
                .name("카테고리")
                .imagePath("/category/new.jpg")
                .build();

        given(categoryRepository.findById(category.getId())).willReturn(Optional.of(category));
        //when
        Long categoryId = categoryCommandService.updateCategory(command);
        //then
        assertThat(categoryId).isEqualTo(category.getId());
        assertThat(category.getName()).isEqualTo("카테고리");
        assertThat(category.getImagePath()).isEqualTo("/category/new.jpg");
        then(categoryRepository).should(never()).existsByParentIdAndNameAndIdNot(any(), any(), any());
    }

    @Test
    @DisplayName("이름을 변경하며 카테고리를 수정한다")
    void updateCategory_whenNameChanged_thenValidatesDuplicateAndUpdates() {
        //given
        Category category = CategoryFixtureBuilder.given().withName("카테고리").build();
        UpdateCategoryCommand command = anUpdateCategoryCommand()
                .id(category.getId())
                .name("새 이름")
                .imagePath("/category/new.jpg")
                .build();

        given(categoryRepository.findById(category.getId())).willReturn(Optional.of(category));
        given(categoryRepository.existsByParentIdAndNameAndIdNot(null, "새 이름", category.getId())).willReturn(false);
        //when
        Long categoryId = categoryCommandService.updateCategory(command);
        //then
        assertThat(categoryId).isEqualTo(category.getId());
        assertThat(category.getName()).isEqualTo("새 이름");
        assertThat(category.getImagePath()).isEqualTo("/category/new.jpg");
    }

    @Test
    @DisplayName("이름 변경시 같은 계층에 동일한 이름이 존재하면 예외가 발생한다")
    void updateCategory_whenSiblingNameDuplicated_thenThrownException() {
        //given
        Category category = CategoryFixtureBuilder.given().withName("카테고리").build();
        UpdateCategoryCommand command = anUpdateCategoryCommand()
                .id(category.getId())
                .name("새 이름")
                .build();

        given(categoryRepository.findById(category.getId())).willReturn(Optional.of(category));
        given(categoryRepository.existsByParentIdAndNameAndIdNot(null, "새 이름", category.getId())).willReturn(true);
        //when
        //then
        assertThatThrownBy(() -> categoryCommandService.updateCategory(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CategoryErrorCode.DUPLICATE_SIBLING_CATEGORY_NAME);
        assertThat(category.getName()).isEqualTo("카테고리");
    }

    @Test
    @DisplayName("수정할 카테고리를 찾을 수 없으면 예외가 발생한다")
    void updateCategory_whenCategoryNotFound_thenThrownException() {
        //given
        Long categoryId = 999L;
        UpdateCategoryCommand command = anUpdateCategoryCommand().id(categoryId).build();

        given(categoryRepository.findById(categoryId)).willReturn(Optional.empty());
        //when
        //then
        assertThatThrownBy(() -> categoryCommandService.updateCategory(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CategoryErrorCode.CATEGORY_NOT_FOUND);
    }

    @Test
    @DisplayName("카테고리의 부모를 변경한다")
    void moveParent_movesToNewParent() {
        //given
        Category oldParent = CategoryFixtureBuilder.given().withName("기존 부모").build();
        Category newParent = CategoryFixtureBuilder.given().withName("새 부모").build();
        Category category = Category.createChild(500L, "카테고리", "/category/image.jpg", oldParent);
        String oldPath = category.getPath();

        given(categoryRepository.findById(category.getId())).willReturn(Optional.of(category));
        given(categoryRepository.findById(newParent.getId())).willReturn(Optional.of(newParent));
        given(categoryRepository.existsByParentIdAndNameAndIdNot(newParent.getId(), category.getName(), category.getId())).willReturn(false);
        given(categoryProductPort.existsProductForCategory(newParent.getId())).willReturn(false);
        given(categoryRepository.findAllByPathStartingWith(oldPath + "/")).willReturn(List.of());
        //when
        Long categoryId = categoryCommandService.moveParent(category.getId(), newParent.getId());
        //then
        assertThat(categoryId).isEqualTo(category.getId());
        assertThat(category.getParent()).isEqualTo(newParent);
        assertThat(category.getDepth()).isEqualTo(newParent.getDepth() + 1);
        assertThat(category.getPath()).isEqualTo(newParent.getPath() + "/" + category.getId());
    }

    @Test
    @DisplayName("카테고리 이동시 하위 카테고리의 경로와 깊이도 함께 갱신된다")
    void moveParent_relocatesDescendants() {
        //given
        Category grandparent = CategoryFixtureBuilder.given().withName("조부모").build();
        Category newParent = Category.createChild(400L, "새 부모", "/category/newParent.jpg", grandparent);
        Category oldParent = CategoryFixtureBuilder.given().withName("기존 부모").build();
        Category category = Category.createChild(500L, "카테고리", "/category/image.jpg", oldParent);
        Category descendant = Category.createChild(600L, "자식", "/category/descendant.jpg", category);
        String oldPath = category.getPath();

        given(categoryRepository.findById(category.getId())).willReturn(Optional.of(category));
        given(categoryRepository.findById(newParent.getId())).willReturn(Optional.of(newParent));
        given(categoryRepository.existsByParentIdAndNameAndIdNot(newParent.getId(), category.getName(), category.getId())).willReturn(false);
        given(categoryProductPort.existsProductForCategory(newParent.getId())).willReturn(false);
        given(categoryRepository.findAllByPathStartingWith(oldPath + "/")).willReturn(List.of(descendant));
        //when
        categoryCommandService.moveParent(category.getId(), newParent.getId());
        //then
        assertThat(descendant.getPath()).isEqualTo(category.getPath() + "/" + descendant.getId());
        assertThat(descendant.getDepth()).isEqualTo(category.getDepth() + 1);
    }

    @Test
    @DisplayName("newParentId가 없으면 최상위로 이동한다")
    void moveParent_toNull_thenBecomesRoot() {
        //given
        Category parent = CategoryFixtureBuilder.given().withName("부모").build();
        Category category = Category.createChild(500L, "카테고리", "/category/image.jpg", parent);
        String oldPath = category.getPath();

        given(categoryRepository.findById(category.getId())).willReturn(Optional.of(category));
        given(categoryRepository.existsByParentIdAndNameAndIdNot(null, category.getName(), category.getId())).willReturn(false);
        given(categoryRepository.findAllByPathStartingWith(oldPath + "/")).willReturn(List.of());
        //when
        categoryCommandService.moveParent(category.getId(), null);
        //then
        assertThat(category.getParent()).isNull();
        assertThat(category.getDepth()).isEqualTo(1);
        assertThat(category.getPath()).isEqualTo(String.valueOf(category.getId()));
        then(categoryProductPort).should(never()).existsProductForCategory(any());
    }

    @Test
    @DisplayName("이동할 카테고리를 찾을 수 없으면 예외가 발생한다")
    void moveParent_whenCategoryNotFound_thenThrownException() {
        //given
        given(categoryRepository.findById(999L)).willReturn(Optional.empty());
        //when
        //then
        assertThatThrownBy(() -> categoryCommandService.moveParent(999L, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CategoryErrorCode.CATEGORY_NOT_FOUND);
    }

    @Test
    @DisplayName("이동할 새 부모 카테고리를 찾을 수 없으면 예외가 발생한다")
    void moveParent_whenNewParentNotFound_thenThrownException() {
        //given
        Category category = CategoryFixtureBuilder.given().build();
        given(categoryRepository.findById(category.getId())).willReturn(Optional.of(category));
        given(categoryRepository.findById(999L)).willReturn(Optional.empty());
        //when
        //then
        assertThatThrownBy(() -> categoryCommandService.moveParent(category.getId(), 999L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CategoryErrorCode.CATEGORY_NOT_FOUND);
    }

    @Test
    @DisplayName("이동 위치에 동일한 이름의 카테고리가 존재하면 예외가 발생한다")
    void moveParent_whenSiblingNameDuplicated_thenThrownException() {
        //given
        Category newParent = CategoryFixtureBuilder.given().withName("새 부모").build();
        Category category = CategoryFixtureBuilder.given().withName("카테고리").build();
        given(categoryRepository.findById(category.getId())).willReturn(Optional.of(category));
        given(categoryRepository.findById(newParent.getId())).willReturn(Optional.of(newParent));
        given(categoryRepository.existsByParentIdAndNameAndIdNot(newParent.getId(), category.getName(), category.getId())).willReturn(true);
        //when
        //then
        assertThatThrownBy(() -> categoryCommandService.moveParent(category.getId(), newParent.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CategoryErrorCode.DUPLICATE_SIBLING_CATEGORY_NAME);
    }

    @Test
    @DisplayName("이동할 새 부모 카테고리에 속한 상품이 존재하면 예외가 발생한다")
    void moveParent_whenNewParentHasProduct_thenThrownException() {
        //given
        Category newParent = CategoryFixtureBuilder.given().withName("새 부모").build();
        Category category = CategoryFixtureBuilder.given().withName("카테고리").build();
        given(categoryRepository.findById(category.getId())).willReturn(Optional.of(category));
        given(categoryRepository.findById(newParent.getId())).willReturn(Optional.of(newParent));
        given(categoryRepository.existsByParentIdAndNameAndIdNot(newParent.getId(), category.getName(), category.getId())).willReturn(false);
        given(categoryProductPort.existsProductForCategory(newParent.getId())).willReturn(true);
        //when
        //then
        assertThatThrownBy(() -> categoryCommandService.moveParent(category.getId(), newParent.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CategoryErrorCode.PARENT_CATEGORY_HAS_PRODUCT);
    }

    @Test
    @DisplayName("리프 카테고리를 삭제한다")
    void deleteCategory_deletesLeafCategory() {
        //given
        Category category = CategoryFixtureBuilder.given().build();
        given(categoryRepository.findById(category.getId())).willReturn(Optional.of(category));
        given(categoryProductPort.existsProductForCategory(category.getId())).willReturn(false);
        //when
        categoryCommandService.deleteCategory(category.getId());
        //then
        then(categoryRepository).should().delete(category);
    }

    @Test
    @DisplayName("삭제할 카테고리를 찾을 수 없으면 예외가 발생한다")
    void deleteCategory_whenNotFound_thenThrownException() {
        //given
        given(categoryRepository.findById(999L)).willReturn(Optional.empty());
        //when
        //then
        assertThatThrownBy(() -> categoryCommandService.deleteCategory(999L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CategoryErrorCode.CATEGORY_NOT_FOUND);
    }

    @Test
    @DisplayName("하위 카테고리가 존재하면 삭제할 수 없다")
    void deleteCategory_whenHasChild_thenThrownException() {
        //given
        Category parent = CategoryFixtureBuilder.given().build();
        Category.createChild(999L, "자식", "/category/child.jpg", parent);
        given(categoryRepository.findById(parent.getId())).willReturn(Optional.of(parent));
        //when
        //then
        assertThatThrownBy(() -> categoryCommandService.deleteCategory(parent.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CategoryErrorCode.CATEGORY_HAS_CHILD);
        then(categoryRepository).should(never()).delete(any());
    }

    @Test
    @DisplayName("카테고리에 속한 상품이 존재하면 삭제할 수 없다")
    void deleteCategory_whenHasProduct_thenThrownException() {
        //given
        Category category = CategoryFixtureBuilder.given().build();
        given(categoryRepository.findById(category.getId())).willReturn(Optional.of(category));
        given(categoryProductPort.existsProductForCategory(category.getId())).willReturn(true);
        //when
        //then
        assertThatThrownBy(() -> categoryCommandService.deleteCategory(category.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CategoryErrorCode.CATEGORY_HAS_PRODUCT);
        then(categoryRepository).should(never()).delete(any());
    }
}
