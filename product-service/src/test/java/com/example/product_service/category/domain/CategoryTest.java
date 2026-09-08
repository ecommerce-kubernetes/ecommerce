package com.example.product_service.category.domain;

import com.example.product_service.category.exception.CategoryErrorCode;
import com.example.product_service.category.fixture.CategoryFixtureBuilder;
import com.example.product_service.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CategoryTest {

    @Test
    @DisplayName("루트 카테고리를 생성한다")
    void createRoot(){
        //given
        Long categoryId = 1L;
        //when
        Category root = Category.createRoot(categoryId, "식품", "/category/food.jpg");
        //then
        assertThat(root.getId()).isEqualTo(categoryId);
        assertThat(root.getName()).isEqualTo("식품");
        assertThat(root.getImagePath()).isEqualTo("/category/food.jpg");
        assertThat(root.getDepth()).isEqualTo(1);
        assertThat(root.getPath()).isEqualTo(String.valueOf(categoryId));
    }

    @Test
    @DisplayName("하위 카테고리를 생성한다.")
    void createChild(){
        //given
        Long categoryId = 10L;
        Category parent = CategoryFixtureBuilder.given().build();
        //when
        Category child = Category.createChild(categoryId, "채소", "/category/vegetable.jpg", parent);
        //then
        assertThat(child.getId()).isEqualTo(categoryId);
        assertThat(child.getName()).isEqualTo("채소");
        assertThat(child.getImagePath()).isEqualTo("/category/vegetable.jpg");
        assertThat(child.getDepth()).isEqualTo(2);
        assertThat(child.getPath()).isEqualTo(parent.getPath()+"/"+ categoryId);
    }

    @Test
    @DisplayName("하위 카테고리를 생성할때 부모 카테고리가 누락되면 예외가 발생한다")
    void createChild_whenParentIsNull_thenThrownException(){
        //given
        Long categoryId = 10L;
        //when
        //then
        assertThatThrownBy(() -> Category.createChild(categoryId, "채소", "/category/vegetable.jpg", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("하위 카테고리 생성시 부모는 필수이다");
    }

    @Test
    @DisplayName("하위 카테고리를 생성할때 부모 카테고리가 최대 depth 인 경우 예외가 발생한다")
    void createChild_whenParentDepthEqualMaxDepth_thenThrownException(){
        //given
        Long categoryId = 10L;
        Category maxDepthParent = CategoryFixtureBuilder.given().buildMaxDepth();
        //when
        //then
        assertThatThrownBy(() -> Category.createChild(categoryId, "채소", "/category/vegetable.jpg", maxDepthParent))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CategoryErrorCode.EXCEED_MAX_DEPTH);
    }

    @Test
    @DisplayName("카테고리의 이름과 이미지 경로를 수정한다")
    void update(){
        //given
        Category category = CategoryFixtureBuilder.given()
                .withName("카테고리")
                .withImagePath("/category/image.jpg")
                .build();
        //when
        category.update("수정", "/category/update.jpg");
        //then
        assertThat(category.getName()).isEqualTo("수정");
        assertThat(category.getImagePath()).isEqualTo("/category/update.jpg");
    }

    @Test
    @DisplayName("부모가 없으면 최상위 카테고리이다")
    void isRoot_whenNoParent_thenReturnsTrue() {
        //given
        Category root = CategoryFixtureBuilder.given().build();
        //when
        //then
        assertThat(root.isRoot()).isTrue();
    }

    @Test
    @DisplayName("부모가 있으면 최상위 카테고리가 아니다")
    void isRoot_whenHasParent_thenReturnsFalse() {
        //given
        Category parent = CategoryFixtureBuilder.given().build();
        Category child = Category.createChild(999L, "자식", "/category/child.jpg", parent);
        //when
        //then
        assertThat(child.isRoot()).isFalse();
    }

    @Test
    @DisplayName("자식이 없으면 리프 카테고리이다")
    void isLeaf_whenNoChildren_thenReturnsTrue() {
        //given
        Category category = CategoryFixtureBuilder.given().build();
        //when
        //then
        assertThat(category.isLeaf()).isTrue();
    }

    @Test
    @DisplayName("자식이 있으면 리프 카테고리가 아니다")
    void isLeaf_whenHasChildren_thenReturnsFalse() {
        //given
        Category parent = CategoryFixtureBuilder.given().build();
        Category.createChild(999L, "자식", "/category/child.jpg", parent);
        //when
        //then
        assertThat(parent.isLeaf()).isFalse();
    }

    @Test
    @DisplayName("카테고리의 부모를 변경하면 depth와 path가 새 부모 기준으로 갱신된다")
    void moveParent_movesToNewParent() {
        //given
        Category oldParent = CategoryFixtureBuilder.given().withName("기존 부모").build();
        Category newParent = CategoryFixtureBuilder.given().withName("새 부모").build();
        Category category = Category.createChild(500L, "카테고리", "/category/image.jpg", oldParent);
        //when
        category.moveParent(newParent);
        //then
        assertThat(category.getParent()).isEqualTo(newParent);
        assertThat(category.getDepth()).isEqualTo(newParent.getDepth() + 1);
        assertThat(category.getPath()).isEqualTo(newParent.getPath() + "/" + category.getId());
        assertThat(oldParent.getChildren()).doesNotContain(category);
        assertThat(newParent.getChildren()).contains(category);
    }

    @Test
    @DisplayName("부모를 null로 변경하면 최상위 카테고리가 된다")
    void moveParent_toNull_thenBecomesRoot() {
        //given
        Category parent = CategoryFixtureBuilder.given().build();
        Category category = Category.createChild(500L, "카테고리", "/category/image.jpg", parent);
        //when
        category.moveParent(null);
        //then
        assertThat(category.getParent()).isNull();
        assertThat(category.getDepth()).isEqualTo(1);
        assertThat(category.getPath()).isEqualTo(String.valueOf(category.getId()));
        assertThat(parent.getChildren()).doesNotContain(category);
    }

    @Test
    @DisplayName("자기 자신을 부모로 설정하면 예외가 발생한다")
    void moveParent_toSelf_thenThrownException() {
        //given
        Category category = CategoryFixtureBuilder.given().build();
        //when
        //then
        assertThatThrownBy(() -> category.moveParent(category))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CategoryErrorCode.CANNOT_SET_SELF_AS_PARENT);
    }

    @Test
    @DisplayName("자신의 하위 카테고리를 부모로 설정하면 예외가 발생한다")
    void moveParent_toDescendant_thenThrownException() {
        //given
        Category category = CategoryFixtureBuilder.given().build();
        Category child = Category.createChild(500L, "자식", "/category/child.jpg", category);
        //when
        //then
        assertThatThrownBy(() -> category.moveParent(child))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CategoryErrorCode.CANNOT_SET_DESCENDANT);
    }

    @Test
    @DisplayName("이동하려는 부모의 depth가 최대이면 예외가 발생한다")
    void moveParent_whenNewParentAtMaxDepth_thenThrownException() {
        //given
        Category category = CategoryFixtureBuilder.given().build();
        Category maxDepthParent = CategoryFixtureBuilder.given().buildMaxDepth();
        //when
        //then
        assertThatThrownBy(() -> category.moveParent(maxDepthParent))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CategoryErrorCode.EXCEED_MAX_DEPTH);
    }

    @Test
    @DisplayName("경로 접두사를 새 접두사로 치환하고 depth를 갱신한다")
    void relocatePrefix_updatesPathAndDepth() {
        //given
        Category grandparent = Category.createRoot(100L, "조부모", "/category/grandparent.jpg");
        Category parent = Category.createChild(200L, "부모", "/category/parent.jpg", grandparent);
        Category descendant = Category.createChild(300L, "자식", "/category/child.jpg", parent);
        //when
        descendant.relocatePrefix("100/200", "999/200", 1);
        //then
        assertThat(descendant.getPath()).isEqualTo("999/200/300");
        assertThat(descendant.getDepth()).isEqualTo(4);
    }

    @Test
    @DisplayName("경로가 예상한 접두사로 시작하지 않으면 예외가 발생한다")
    void relocatePrefix_whenPathDoesNotStartWithPrefix_thenThrownException() {
        //given
        Category category = CategoryFixtureBuilder.given().build();
        //when
        //then
        assertThatThrownBy(() -> category.relocatePrefix("존재하지-않는-접두사", "새-접두사", 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("경로(path)를 기준으로 루트부터 자신까지의 식별자 목록을 반환한다")
    void getAncestorIds_returnsIdsFromRootToSelf() {
        //given
        Category grandparent = Category.createRoot(1L, "조부모", "/category/grandparent.jpg");
        Category parent = Category.createChild(2L, "부모", "/category/parent.jpg", grandparent);
        Category category = Category.createChild(3L, "카테고리", "/category/category.jpg", parent);
        //when
        List<Long> ancestorIds = category.getAncestorIds();
        //then
        assertThat(ancestorIds).containsExactly(1L, 2L, 3L);
    }

    @Test
    @DisplayName("최상위 카테고리는 자기 자신의 식별자만 반환한다")
    void getAncestorIds_whenRoot_thenReturnsOnlySelfId() {
        //given
        Category root = Category.createRoot(1L, "루트", "/category/root.jpg");
        //when
        List<Long> ancestorIds = root.getAncestorIds();
        //then
        assertThat(ancestorIds).containsExactly(1L);
    }
}
