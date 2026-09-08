package com.example.product_service.category.domain;

import com.example.product_service.category.exception.CategoryErrorCode;
import com.example.product_service.category.fixture.CategoryFixtureBuilder;
import com.example.product_service.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
}
