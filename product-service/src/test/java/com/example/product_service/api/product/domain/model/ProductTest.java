package com.example.product_service.api.product.domain.model;

import com.example.product_service.api.category.domain.model.Category;
import com.example.product_service.api.common.exception.BusinessException;
import com.example.product_service.api.common.exception.ProductErrorCode;
import com.example.product_service.api.option.domain.model.OptionType;
import com.example.product_service.api.option.domain.model.OptionValue;
import com.example.product_service.support.fixture.builder.ProductTestBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.tuple;

public class ProductTest {

    @Nested
    @DisplayName("상품을 생성한다")
    class Create {
        @Test
        @DisplayName("상품을 생성한다")
        void create(){
            //given
            Category category = Category.create("카테고리", null, "http://image.jpg");
            //when
            Product product = Product.create("상품", "상품 설명", category);
            //then
            assertThat(product)
                    .extracting(Product::getName, Product::getCategory, Product::getStatus, Product::getDescription,
                            Product::getPublishedAt, Product::getThumbnail, Product::getRating, Product::getReviewCount, Product::getPopularityScore,
                            Product::getLowestPrice, Product::getOriginalPrice, Product::getMaxDiscountRate)
                    .containsExactly(
                            "상품", category, ProductStatus.PREPARING, "상품 설명",
                            null, null, 0.0, 0L, 0.0,
                            null, null, null
                    );
        }

        @Test
        @DisplayName("상품은 반드시 하나의 카테고리에 속해야 한다")
        void create_category_null(){
            //given
            //when
            //then
            assertThatThrownBy(() -> Product.create("상품", "상품 설명", null))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.PRODUCT_CATEGORY_REQUIRED);
        }

        @Test
        @DisplayName("상품은 최하위 카테고리에 속해야 한다")
        void create_category_not_leaf(){
            //given
            Category electronics = Category.create("전자기기", null, "http://electronics.jpg");
            Category.create("핸드폰", electronics, "http://cellPhone.jpg");
            //when
            //then
            assertThatThrownBy(() -> Product.create("상품", "상품 설명", electronics))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.CATEGORY_NOT_LEAF);
        }
    }

    @Nested
    @DisplayName("상품 옵션 설정")
    class UpdateOptions {

        @Test
        @DisplayName("상품 옵션을 설정한다")
        void updateOptions(){
            //given
            OptionType size = createOptionType(1L, "사이즈", List.of("XL", "L"));
            OptionType color = createOptionType(2L, "색상", List.of("RED", "BLUE"));
            Product product = ProductTestBuilder.aProduct().build();
            //when
            product.updateOptions(List.of(size, color));
            //then
            assertThat(product.getOptions()).hasSize(2)
                    .extracting(ProductOption::getOptionType, ProductOption::getPriority)
                    .containsExactly(
                            tuple(size, 1),
                            tuple(color, 2)
                    );
        }

        @Test
        @DisplayName("판매중인 상품에 상품 옵션을 설정할 수 없다")
        void updateOptions_on_sale(){
            //given
            OptionType size = createOptionType(1L, "사이즈", List.of("XL", "L"));
            Product product = ProductTestBuilder.aProduct().withStatus(ProductStatus.ON_SALE).build();
            //when
            //then
            assertThatThrownBy(() -> product.updateOptions(List.of(size)))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.CANNOT_MODIFY_PRODUCT_OPTION_ON_SALE);
        }

        @Test
        @DisplayName("상품에 상품 변형이 존재하면 옵션을 설정할 수 없다")
        void updateOptions_has_variants(){
            //given
            OptionType size = createOptionType(1L, "사이즈", List.of("XL", "L"));
            ProductVariant mockVariant = Mockito.mock(ProductVariant.class);
            Product product = ProductTestBuilder.aProduct()
                    .withVariants(List.of(mockVariant))
                    .build();
            //when
            //then
            assertThatThrownBy(() -> product.updateOptions(List.of(size)))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.CANNOT_MODIFY_PRODUCT_OPTION_HAS_VARIANTS);
        }

        @Test
        @DisplayName("옵션은 최대 3개까지 설정 가능하다")
        void updateOptions_exceed_options_count(){
            //given
            OptionType size = createOptionType(1L, "사이즈", List.of("XL", "L"));
            OptionType color = createOptionType(2L, "색상", List.of("RED", "BLUE"));
            OptionType texture = createOptionType(3L, "재질", List.of("WOOL", "COTTON"));
            OptionType storage = createOptionType(4L, "용량", List.of("256GB", "128GB"));
            Product product = ProductTestBuilder.aProduct().build();
            //when
            //then
            assertThatThrownBy(() -> product.updateOptions(List.of(size, color, texture, storage)))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.EXCEED_PRODUCT_OPTION_COUNT);
        }

        @Test
        @DisplayName("상품 옵션은 중복될 수 없다")
        void updateOptions_duplicate_options(){
            //given
            OptionType size = createOptionType(1L, "사이즈", List.of("XL", "L"));
            Category category = Category.create("카테고리", null, "http://image.jpg");
            Product product = Product.create("상품", "상품 설명", category);
            //when
            //then
            assertThatThrownBy(() -> product.updateOptions(List.of(size, size)))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.DUPLICATE_OPTION_TYPE);
        }
    }

    @Test
    @DisplayName("상품 상태가 삭제 상태라면 상품 변형을 추가할 수 없다")
    void validateCreatableVariantStatus(){
        //given
        Category category = Category.create("카테고리", null, "http://image.jpg");
        Product product = Product.create("상품", "상품 설명", category);
        product.deleted();
        //when
        //then
        assertThatThrownBy(product::validateCreatableVariantStatus)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("상품의 옵션 스펙 개수와 요청 옵션 값의 개수가 서로 다르면 예외를 던진다")
    void validateAndSortOptionValues_not_match_optionSpec_size(){
        //given
        OptionType size = createOptionType(1L, "사이즈", List.of("XL", "L"));
        OptionType color = createOptionType(2L, "색상", List.of("RED", "BLUE"));
        OptionType texture = createOptionType(3L, "재질", List.of("WOOL", "COTTON"));
        OptionValue xl = findOptionValueByName(size, "XL");
        OptionValue blue = findOptionValueByName(color, "BLUE");
        OptionValue cotton = findOptionValueByName(texture, "COTTON");

        Category category = Category.create("카테고리", null, "http://image.jpg");
        Product product = Product.create("상품", "상품 설명", category);
        product.updateOptions(List.of(size, color));
        //when
        //then
        assertThatThrownBy(() -> product.validateAndSortOptionValues(List.of(xl, blue, cotton)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ProductErrorCode.NOT_MATCH_PRODUCT_OPTION_SPEC);
    }

    @Test
    @DisplayName("요청 옵션 값이 상품 옵션 스펙의 옵션 타입에 맞지 않으면 예외를 던진다")
    void validateAndSortOptionValues_illegalOptionValue_optionSpec_optionType(){
        //given
        OptionType size = createOptionType(1L, "사이즈", List.of("XL", "L"));
        OptionType color = createOptionType(2L, "색상", List.of("RED", "BLUE"));
        OptionType texture = createOptionType(3L, "재질", List.of("WOOL", "COTTON"));
        OptionValue xl = findOptionValueByName(size, "XL");
        OptionValue cotton = findOptionValueByName(texture, "COTTON");

        Category category = Category.create("카테고리", null, "http://image.jpg");
        Product product = Product.create("상품", "상품 설명", category);
        product.updateOptions(List.of(size, color));
        //when
        //then
        assertThatThrownBy(() -> product.validateAndSortOptionValues(List.of(xl, cotton)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ProductErrorCode.NOT_MATCH_PRODUCT_OPTION_SPEC);
    }

    @Test
    @DisplayName("동일한 옵션조합의 상품 변형을 추가할 수 없다")
    void addVariant_duplicateVariant(){
        //given
        OptionType size = OptionType.create("사이즈", List.of("XL", "L"));
        OptionValue xl = findOptionValueByName(size, "XL");
        Category category = Category.create("카테고리", null, "http://image.jpg");
        Product product = Product.create("상품", "상품 설명", category);
        product.updateOptions(List.of(size));
        ProductVariant variant = ProductVariant.create("TEST", 3000L, 100, 10);
        variant.addProductVariantOptions(List.of(xl));
        product.addVariant(variant);
        //when
        //then
        assertThatThrownBy(() -> product.addVariant(variant))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ProductErrorCode.PRODUCT_HAS_DUPLICATE_VARIANT);
    }

    private OptionType createOptionType(Long id, String name, List<String> values) {
        OptionType optionType = OptionType.create(name, values);
        ReflectionTestUtils.setField(optionType, "id", id);
        return optionType;
    }
    
    private OptionValue findOptionValueByName(OptionType optionType, String name) {
        return optionType.getOptionValues().stream()
                .filter(optionValue -> optionValue.getName().equals(name))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
