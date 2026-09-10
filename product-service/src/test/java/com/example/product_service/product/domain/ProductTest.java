package com.example.product_service.product.domain;

import com.example.product_service.common.exception.BusinessException;
import com.example.product_service.product.domain.context.CreateProductContext;
import com.example.product_service.product.domain.context.RegisterOptionTypeContext;
import com.example.product_service.product.domain.context.UpdateProductContext;
import com.example.product_service.product.exception.ProductErrorCode;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

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

    @Test
    @DisplayName("상품의 이름, 카테고리, 설명을 수정한다")
    void update() {
        //given
        Product product = Product.create(
                CreateProductContext.builder()
                        .id(1L)
                        .categoryId(10L)
                        .name("상품")
                        .description("상품 설명")
                        .build()
        );
        UpdateProductContext context = UpdateProductContext.builder()
                .name("변경된 상품")
                .categoryId(20L)
                .description("변경된 상품 설명")
                .build();
        //when
        product.update(context);
        //then
        assertThat(product.getName()).isEqualTo("변경된 상품");
        assertThat(product.getCategoryId()).isEqualTo(20L);
        assertThat(product.getDescription()).isEqualTo("변경된 상품 설명");
    }

    @Test
    @DisplayName("상품 수정시 이름이 없으면 예외가 발생한다")
    void update_whenNameIsBlank_thenThrownException() {
        //given
        Product product = Product.create(
                CreateProductContext.builder()
                        .id(1L)
                        .categoryId(10L)
                        .name("상품")
                        .description("상품 설명")
                        .build()
        );
        UpdateProductContext context = UpdateProductContext.builder()
                .name(" ")
                .categoryId(20L)
                .description("변경된 상품 설명")
                .build();
        //when
        //then
        assertThatThrownBy(() -> product.update(context))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품 이름은 필수이다");
    }

    @Test
    @DisplayName("상품 수정시 카테고리가 없으면 예외가 발생한다")
    void update_whenCategoryIdIsNull_thenThrownException() {
        //given
        Product product = Product.create(
                CreateProductContext.builder()
                        .id(1L)
                        .categoryId(10L)
                        .name("상품")
                        .description("상품 설명")
                        .build()
        );
        UpdateProductContext context = UpdateProductContext.builder()
                .name("변경된 상품")
                .categoryId(null)
                .description("변경된 상품 설명")
                .build();
        //when
        //then
        assertThatThrownBy(() -> product.update(context))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품 카테고리는 필수이다");
    }

    @Test
    @DisplayName("상품 옵션 타입을 등록한다")
    void registerOptionTypes() {
        //given
        Product product = Product.create(
                CreateProductContext.builder()
                        .id(1L)
                        .categoryId(10L)
                        .name("상품")
                        .description("상품 설명")
                        .build()
        );
        List<RegisterOptionTypeContext> contexts = List.of(
                RegisterOptionTypeContext.builder().id(1L).optionTypeId(10L).build(),
                RegisterOptionTypeContext.builder().id(2L).optionTypeId(20L).build()
        );
        //when
        product.registerOptionTypes(contexts);
        //then
        assertThat(product.getProductOptionTypes()).hasSize(2);
        assertThat(product.getProductOptionTypes())
                .extracting("optionTypeId", "displayOrder")
                .containsExactly(
                        Tuple.tuple(10L, 1),
                        Tuple.tuple(20L, 2)
                );
    }

    @Test
    @DisplayName("판매중이거나 삭제된 상품은 옵션 타입을 등록할 수 없다")
    void registerOptionTypes_whenProductDeleted_thenThrownException() {
        //given
        Product product = Product.create(
                CreateProductContext.builder()
                        .id(1L)
                        .categoryId(10L)
                        .name("상품")
                        .description("상품 설명")
                        .build()
        );
        product.deleted(LocalDateTime.now());

        List<RegisterOptionTypeContext> contexts = List.of(
                RegisterOptionTypeContext.builder().id(1L).optionTypeId(10L).build()
        );
        //when
        //then
        assertThatThrownBy(() -> product.registerOptionTypes(contexts))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ProductErrorCode.CANNOT_REGISTER_OPTION_TYPE);
    }

    @Test
    @DisplayName("옵션 타입은 최대 3개까지만 등록할 수 있다")
    void registerOptionTypes_whenExceedMaxSize_thenThrownException() {
        //given
        Product product = Product.create(
                CreateProductContext.builder()
                        .id(1L)
                        .categoryId(10L)
                        .name("상품")
                        .description("상품 설명")
                        .build()
        );
        List<RegisterOptionTypeContext> contexts = List.of(
                RegisterOptionTypeContext.builder().id(1L).optionTypeId(10L).build(),
                RegisterOptionTypeContext.builder().id(2L).optionTypeId(20L).build(),
                RegisterOptionTypeContext.builder().id(3L).optionTypeId(30L).build(),
                RegisterOptionTypeContext.builder().id(4L).optionTypeId(40L).build()
        );
        //when
        //then
        assertThatThrownBy(() -> product.registerOptionTypes(contexts))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ProductErrorCode.EXCEED_MAX_OPTION_SIZE);
    }

    @Test
    @DisplayName("중복된 옵션 타입은 등록할 수 없다")
    void registerOptionTypes_whenOptionTypeDuplicated_thenThrownException() {
        //given
        Product product = Product.create(
                CreateProductContext.builder()
                        .id(1L)
                        .categoryId(10L)
                        .name("상품")
                        .description("상품 설명")
                        .build()
        );
        List<RegisterOptionTypeContext> contexts = List.of(
                RegisterOptionTypeContext.builder().id(1L).optionTypeId(10L).build(),
                RegisterOptionTypeContext.builder().id(2L).optionTypeId(10L).build()
        );
        //when
        //then
        assertThatThrownBy(() -> product.registerOptionTypes(contexts))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ProductErrorCode.OPTION_TYPE_DUPLICATED);
    }
}
