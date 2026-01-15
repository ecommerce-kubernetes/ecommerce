package com.example.product_service.api.product.domain.model;

import com.example.product_service.api.category.domain.model.Category;
import com.example.product_service.api.common.exception.BusinessException;
import com.example.product_service.api.common.exception.ProductErrorCode;
import com.example.product_service.api.option.domain.model.OptionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.tuple;

public class ProductTest {

    @Test
    @DisplayName("상품을 생성한다")
    void create(){
        //given
        Category category = Category.create("카테고리", null, "http://image.jpg");
        //when
        Product product = Product.create("상품", "상품설명", category);
        //then
        assertThat(product)
                .extracting(
                        Product::getName, Product::getCategory, Product::getStatus, Product::getDescription, Product::getPublishedAt,
                        Product::getThumbnail, Product::getRating, Product::getReviewCount, Product::getDisplayPrice, Product::getOriginalPrice,
                        Product::getMaxDiscountRate)
                .containsExactly(
                        "상품", category, ProductStatus.PREPARING, "상품설명", null,
                        null, 0.0, 0L, null, null, null
                );

    }

    @Test
    @DisplayName("상품을 생성할때 카테고리는 최하위 카테고리여야 한다")
    void create_category_null(){
        //given
        Category parent = Category.create("카테고리", null, "http://image.jpg");
        Category child = Category.create("카테고리", parent, "http://image.jpg");
        //when
        //then
        assertThatThrownBy(() -> Product.create("상품", "상품 설명", parent))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ProductErrorCode.CATEGORY_NOT_LEAF);
    }

    @Test
    @DisplayName("상품 옵션은 최대 3개까지 설정 가능하다")
    void updateOptionSpecs_exceed_option_type(){
        //given
        OptionType optionType1 = createOptionType(1L, "사이즈", List.of("XL", "L"));
        OptionType optionType2 = createOptionType(2L, "색상", List.of("RED", "BLUE"));
        OptionType optionType3 = createOptionType(3L, "재질", List.of("WOOL", "COTTON"));
        OptionType optionType4 = createOptionType(4L, "용량", List.of("256GB", "128GB"));
        Category category = Category.create("카테고리", null, "http://image.jpg");
        Product product = Product.create("상품", "상품 설명", category);
        //when
        //then
        assertThatThrownBy(() -> product.updateOptionSpecs(List.of(optionType1, optionType2, optionType3, optionType4)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ProductErrorCode.EXCEED_OPTION_SPEC_COUNT);
    }

    @Test
    @DisplayName("상품 옵션을 설정한다")
    void updateOptionSpecs(){
        //given
        OptionType optionType = createOptionType(1L, "사이즈", List.of("XL", "L"));
        Category category = Category.create("카테고리", null, "http://image.jpg");
        Product product = Product.create("상품", "상품 설명", category);
        //when
        product.updateOptionSpecs(List.of(optionType));
        //then
        assertThat(product.getOptionSpecs()).hasSize(1)
                .extracting(ProductOptionSpec::getOptionType, ProductOptionSpec::getPriority)
                .containsExactly(
                        tuple(optionType, 1)
                );
    }

    @Test
    @DisplayName("상품 옵션은 중복될 수 없다")
    void updateOptionSpecs_duplicate_optionType(){
        //given
        OptionType optionType = createOptionType(1L, "사이즈", List.of("XL", "L"));
        Category category = Category.create("카테고리", null, "http://image.jpg");
        Product product = Product.create("상품", "상품 설명", category);
        //when
        //then
        assertThatThrownBy(() -> product.updateOptionSpecs(List.of(optionType, optionType)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ProductErrorCode.DUPLICATE_OPTION_TYPE);
    }

    private OptionType createOptionType(Long id, String name, List<String> values) {
        OptionType optionType = OptionType.create(name, values);
        ReflectionTestUtils.setField(optionType, "id", id);
        return optionType;
    }
}
