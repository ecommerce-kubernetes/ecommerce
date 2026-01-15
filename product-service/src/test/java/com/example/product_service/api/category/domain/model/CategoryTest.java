package com.example.product_service.api.category.domain.model;

import com.example.product_service.api.common.exception.BusinessException;
import com.example.product_service.api.common.exception.CategoryErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CategoryTest {

    @Test
    @DisplayName("카테고리 생성시 parent 가 null 이면 depth가 1인 카테고리를 생성한다")
    void create_root(){
        //given
        //when
        Category category = Category.create("카테고리", null, "http://image.jpg");
        //then
        assertThat(category)
                .extracting(Category::getName, Category::getParent, Category::getDepth, Category::getImageUrl)
                .containsExactly(
                        "카테고리", null, 1, "http://image.jpg"
                );
    }

    @Test
    @DisplayName("자식 카테고리 생성시 depth는 부모의 depth + 1 이다")
    void create_child(){
        //given
        Category root = Category.create("루트", null, "http://image.jpg");
        //when
        Category category = Category.create("자식", root, "http://image.jpg");
        //then
        assertThat(category)
                .extracting(Category::getName, Category::getParent, Category::getDepth, Category::getImageUrl)
                .containsExactly("자식", root, 2, "http://image.jpg");
    }

    @Test
    @DisplayName("카테고리 이름을 변경한다")
    void rename(){
        //given
        Category category = Category.create("카테고리", null, "http://image.jpg");
        //when
        category.rename("새 카테고리");
        //then
        assertThat(category.getName()).isEqualTo("새 카테고리");
    }

    @Test
    @DisplayName("변경할 이름이 유효하지 않으면 예외를 던진다")
    void rename_invalidName(){
        //given
        Category category = Category.create("카테고리", null, "http://image.jpg");
        //when
        //then
        assertThatThrownBy(() -> category.rename(" "))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CategoryErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("카테고리 이미지를 변경한다")
    void changeImage(){
        //given
        Category category = Category.create("카테고리", null, "http://image.jpg");
        //when
        category.changeImage("http://newImage.jpg");
        //then
        assertThat(category.getImageUrl()).isEqualTo("http://newImage.jpg");
    }

    @Test
    @DisplayName("변경할 이미지가 유효하지 않으면 예외를 던진다")
    void changeImage_invalidImage(){
        //given
        Category category = Category.create("카테고리", null, "http://image.jpg");
        //when
        //then
        assertThatThrownBy(() -> category.changeImage("  "))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CategoryErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("카테고리가 최상위 카테고리면 true를 반환한다")
    void isRoot_true(){
        //given
        Category category = Category.create("카테고리", null, "http://image.jpg");
        //when
        boolean isRoot = category.isRoot();
        //then
        assertThat(isRoot).isTrue();
    }

    @Test
    @DisplayName("카테고리가 최상위 카테고리가 아니면 fals를 반환한다")
    void isRoot_false(){
        //given
        Category root = Category.create("루트", null, "http://image.jpg");
        Category category = Category.create("자식", root, "http://image.jpg");
        //when
        boolean isRoot = category.isRoot();
        //then
        assertThat(isRoot).isFalse();
    }

    @Test
    @DisplayName("카테고리 경로를 생성한다")
    void generatePath(){
        //given
        Category root = Category.create("루트", null, "http://image.jpg");
        setId(root, 1L);
        //when
        root.generatePath();
        //then
        assertThat(root.getPath()).isEqualTo(String.valueOf(1L));
    }

    @Test
    @DisplayName("자식 카테고리 경로를 생성한다")
    void generatePath_child(){
        //given
        Category root = Category.create("루트", null, "http://image.jpg");
        setId(root, 1L);
        root.generatePath();
        Category child = Category.create("자식", root, "http://image.jpg");
        setId(child, 2L);
        //when
        child.generatePath();
        //then
        assertThat(child.getPath()).isEqualTo(root.getId() + "/" + child.getId());
    }

    @Test
    @DisplayName("조상 id를 조회한다")
    void getAncestorsIds(){
        //given
        Category root = Category.create("루트", null, "http://image.jpg");
        setId(root, 1L);
        root.generatePath();
        Category child = Category.create("자식", root, "http://image.jpg");
        setId(child, 2L);
        child.generatePath();
        //when
        List<Long> ids = child.getAncestorsIds();
        //then
        assertThat(ids).contains(1L, 2L);
    }

    @Test
    @DisplayName("카테고리 부모를 변경한다")
    void moveParent(){
        //given
        Category root = Category.create("루트", null, "http://image.jpg");
        setId(root, 1L);
        root.generatePath();
        Category child = Category.create("자식", root, "http://image.jpg");
        setId(child, 2L);
        child.generatePath();
        Category grandson = Category.create("손자", child, "http://image.jpg");
        setId(grandson, 3L);
        grandson.generatePath();

        Category newParent = Category.create("새 부모", null, "http://image.jpg");
        setId(newParent, 4L);
        newParent.generatePath();
        //when
        child.moveParent(newParent);
        //then
        assertThat(child.getParent()).isEqualTo(newParent);
        assertThat(child.getPath()).isEqualTo(newParent.getId() + "/" + child.getId());
        assertThat(grandson.getPath()).isEqualTo(newParent.getId() + "/" + child.getId() + "/" + grandson.getId());
    }

    @Test
    @DisplayName("최하위 카테고리인 경우 true를 반환한다")
    void isLeaf(){
        //given
        Category root = Category.create("루트", null, "http://image.jpg");
        //when
        boolean isLeaf = root.isLeaf();
        //then
        assertThat(isLeaf).isTrue();
    }

    private void setId(Category category, Long id) {
        ReflectionTestUtils.setField(category, "id", id);
    }
}
