package com.example.product_service.product.domain;

import com.example.product_service.product.domain.context.CreateProductContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ProductTest {

    @Test
    @DisplayName("상품을 생성한다")
    void create() {
        //given
        CreateProductContext context = CreateProductContext.builder()
                .id(1L)
                .categoryId(10L)
                .name("상품")
                .description("상품 설명")
                .build();
        //when
        Product product = Product.create(context);
        //then
        assertThat(product.getId()).isEqualTo(1L);
        assertThat(product.getName()).isEqualTo("상품");
        assertThat(product.getCategoryId()).isEqualTo(10L);
        assertThat(product.getDescription()).isEqualTo("상품 설명");
        assertThat(product.getStatus()).isEqualTo(ProductStatus.PREPARING);
        assertThat(product.getRating()).isEqualTo(0.0);
        assertThat(product.getReviewCount()).isEqualTo(0L);
        assertThat(product.getPopularityScore()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("상품 생성시 식별자가 없으면 예외가 발생한다")
    void create_whenIdIsNull_thenThrownException() {
        //given
        CreateProductContext context = CreateProductContext.builder()
                .id(null)
                .categoryId(10L)
                .name("상품")
                .description("상품 설명")
                .build();
        //when
        //then
        assertThatThrownBy(() -> Product.create(context))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품 아이디는 필수이다");
    }

    @Test
    @DisplayName("상품 생성시 이름이 없으면 예외가 발생한다")
    void create_whenNameIsBlank_thenThrownException() {
        //given
        CreateProductContext context = CreateProductContext.builder()
                .id(1L)
                .categoryId(10L)
                .name(" ")
                .description("상품 설명")
                .build();
        //when
        //then
        assertThatThrownBy(() -> Product.create(context))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품 이름은 필수이다");
    }

    @Test
    @DisplayName("상품 생성시 카테고리가 없으면 예외가 발생한다")
    void create_whenCategoryIdIsNull_thenThrownException() {
        //given
        CreateProductContext context = CreateProductContext.builder()
                .id(1L)
                .categoryId(null)
                .name("상품")
                .description("상품 설명")
                .build();
        //when
        //then
        assertThatThrownBy(() -> Product.create(context))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품 카테고리는 필수이다");
    }
}
