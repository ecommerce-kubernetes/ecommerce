package com.example.product_service.category.application.service;

import com.example.product_service.category.application.port.CategoryRepository;
import com.example.product_service.category.application.service.dto.result.ChildCategoriesResult;
import com.example.product_service.category.application.service.dto.result.DetailCategoryResult;
import com.example.product_service.category.application.service.dto.result.RootCategoriesResult;
import com.example.product_service.category.application.service.dto.result.TreeCategoriesResult;
import com.example.product_service.category.domain.Category;
import com.example.product_service.category.exception.CategoryErrorCode;
import com.example.product_service.category.fixture.CategoryFixtureBuilder;
import com.example.product_service.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CategoryQueryServiceTest {

    @InjectMocks
    private CategoryQueryService categoryQueryService;

    @Mock
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("최상위 카테고리 목록을 조회한다")
    void getRoots() {
        //given
        Category root1 = CategoryFixtureBuilder.given().withName("식품").build();
        Category root2 = CategoryFixtureBuilder.given().withName("가구").build();
        given(categoryRepository.findAllByParentIsNull()).willReturn(List.of(root1, root2));
        //when
        RootCategoriesResult result = categoryQueryService.getRoots();
        //then
        assertThat(result.categories()).hasSize(2);
        assertThat(result.categories().getFirst().id()).isEqualTo(root1.getId());
        assertThat(result.categories().getFirst().name()).isEqualTo(root1.getName());
        assertThat(result.categories().getFirst().isLeaf()).isTrue();
        assertThat(result.categories().getFirst().id()).isEqualTo(root2.getId());
    }

    @Test
    @DisplayName("최상위 카테고리가 없으면 빈 목록을 반환한다")
    void getRoots_whenNoRoots_thenReturnsEmptyList() {
        //given
        given(categoryRepository.findAllByParentIsNull()).willReturn(List.of());
        //when
        RootCategoriesResult result = categoryQueryService.getRoots();
        //then
        assertThat(result.categories()).isEmpty();
    }

    @Test
    @DisplayName("카테고리의 자식 목록을 조회한다")
    void getChildren() {
        //given
        Category parent = CategoryFixtureBuilder.given().withName("전자기기").build();
        Category child = Category.createChild(999L, "노트북", "/category/laptop.jpg", parent);
        given(categoryRepository.existsById(parent.getId())).willReturn(true);
        given(categoryRepository.findAllByParentId(parent.getId())).willReturn(List.of(child));
        //when
        ChildCategoriesResult result = categoryQueryService.getChildren(parent.getId());
        //then
        assertThat(result.categories()).hasSize(1);
        assertThat(result.categories().getFirst().id()).isEqualTo(child.getId());
        assertThat(result.categories().getFirst().name()).isEqualTo(child.getName());
        assertThat(result.categories().getFirst().isLeaf()).isTrue();
    }

    @Test
    @DisplayName("자식이 없으면 빈 목록을 반환한다")
    void getChildren_whenNoChildren_thenReturnsEmptyList() {
        //given
        Category parent = CategoryFixtureBuilder.given().build();
        given(categoryRepository.existsById(parent.getId())).willReturn(true);
        given(categoryRepository.findAllByParentId(parent.getId())).willReturn(List.of());
        //when
        ChildCategoriesResult result = categoryQueryService.getChildren(parent.getId());
        //then
        assertThat(result.categories()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 카테고리의 자식 목록을 조회하면 예외가 발생한다")
    void getChildren_whenCategoryNotFound_thenThrownException() {
        //given
        given(categoryRepository.existsById(999L)).willReturn(false);
        //when
        //then
        assertThatThrownBy(() -> categoryQueryService.getChildren(999L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CategoryErrorCode.CATEGORY_NOT_FOUND);
    }

    @Test
    @DisplayName("카테고리 상세 정보와 브레드크럼을 조회한다")
    void getDetail() {
        //given
        Category root = Category.createRoot(1L, "전자기기", "/category/electronics.jpg");
        Category child = Category.createChild(2L, "노트북", "/category/laptop.jpg", root);
        given(categoryRepository.findById(child.getId())).willReturn(Optional.of(child));
        given(categoryRepository.findAllById(child.getAncestorIds())).willReturn(List.of(root, child));
        //when
        DetailCategoryResult result = categoryQueryService.getDetail(child.getId());
        //then
        assertThat(result.id()).isEqualTo(child.getId());
        assertThat(result.name()).isEqualTo(child.getName());
        assertThat(result.depth()).isEqualTo(child.getDepth());
        assertThat(result.isLeaf()).isTrue();
        assertThat(result.breadcrumb()).hasSize(2);
        assertThat(result.breadcrumb().getFirst().id()).isEqualTo(root.getId());
        assertThat(result.breadcrumb().get(1).id()).isEqualTo(child.getId());
    }

    @Test
    @DisplayName("최상위 카테고리 상세 조회시 브레드크럼은 자기 자신만 포함한다")
    void getDetail_whenRoot_thenBreadcrumbContainsOnlySelf() {
        //given
        Category root = Category.createRoot(1L, "전자기기", "/category/electronics.jpg");
        given(categoryRepository.findById(root.getId())).willReturn(Optional.of(root));
        given(categoryRepository.findAllById(root.getAncestorIds())).willReturn(List.of(root));
        //when
        DetailCategoryResult result = categoryQueryService.getDetail(root.getId());
        //then
        assertThat(result.breadcrumb()).hasSize(1);
        assertThat(result.breadcrumb().getFirst().id()).isEqualTo(root.getId());
    }

    @Test
    @DisplayName("상세 조회시 카테고리를 찾을 수 없으면 예외가 발생한다")
    void getDetail_whenNotFound_thenThrownException() {
        //given
        given(categoryRepository.findById(999L)).willReturn(Optional.empty());
        //when
        //then
        assertThatThrownBy(() -> categoryQueryService.getDetail(999L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CategoryErrorCode.CATEGORY_NOT_FOUND);
    }

    @Test
    @DisplayName("전체 카테고리를 트리 구조로 조회한다")
    void getTree() {
        //given
        Category root = CategoryFixtureBuilder.given().withName("전자기기").build();
        Category child = Category.createChild(999L, "노트북", "/category/laptop.jpg", root);
        given(categoryRepository.findAll()).willReturn(List.of(root, child));
        //when
        TreeCategoriesResult result = categoryQueryService.getTree();
        //then
        assertThat(result.categories()).hasSize(1);
        TreeCategoriesResult.TreeCategoryResult rootNode = result.categories().getFirst();
        assertThat(rootNode.id()).isEqualTo(root.getId());
        assertThat(rootNode.isLeaf()).isFalse();
        assertThat(rootNode.children()).hasSize(1);
        assertThat(rootNode.children().getFirst().id()).isEqualTo(child.getId());
        assertThat(rootNode.children().getFirst().isLeaf()).isTrue();
    }

    @Test
    @DisplayName("카테고리가 없으면 빈 트리를 반환한다")
    void getTree_whenNoCategories_thenReturnsEmptyTree() {
        //given
        given(categoryRepository.findAll()).willReturn(List.of());
        //when
        TreeCategoriesResult result = categoryQueryService.getTree();
        //then
        assertThat(result.categories()).isEmpty();
    }
}
