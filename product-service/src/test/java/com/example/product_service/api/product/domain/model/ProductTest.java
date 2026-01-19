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

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.tuple;

public class ProductTest {

    private OptionType createOptionType(Long id, String name, List<String> values) {
        OptionType optionType = OptionType.create(name, values);
        ReflectionTestUtils.setField(optionType, "id", id);
        return optionType;
    }

    private OptionValue getOptionValueSettingId(Long id, OptionType optionType, String name) {
        OptionValue optionValue = optionType.getOptionValues().stream()
                .filter(v -> v.getName().equals(name))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
        ReflectionTestUtils.setField(optionValue, "id", id);
        return optionValue;
    }

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
                    .isEqualTo(ProductErrorCode.OPTION_MODIFICATION_NOT_ALLOWED_ON_SALE);
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
                    .isEqualTo(ProductErrorCode.OPTION_MODIFICATION_NOT_ALLOWED_WITH_VARIANT);
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
                    .isEqualTo(ProductErrorCode.OPTION_COUNT_LIMIT_EXCEEDED);
        }
        @Test
        @DisplayName("상품 옵션은 중복될 수 없다")
        void updateOptions_duplicate_options(){
            //given
            OptionType size = createOptionType(1L, "사이즈", List.of("XL", "L"));
            Product product = ProductTestBuilder.aProduct().build();
            //when
            //then
            assertThatThrownBy(() -> product.updateOptions(List.of(size, size)))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.OPTION_TYPE_DUPLICATED);
        }
    }

    @Nested
    @DisplayName("상품 변형 추가")
    class AddVariant {

        @Test
        @DisplayName("상품 변형을 추가한다")
        void addVariant(){
            //given
            OptionType size = createOptionType(1L, "사이즈", List.of("XL", "L"));
            OptionValue xl = getOptionValueSettingId(1L, size, "XL");
            OptionValue l = getOptionValueSettingId(2L, size, "L");
            ProductVariant existVariant = ProductVariant.create("TEST", 3000L, 100, 40);
            existVariant.addProductVariantOptions(List.of(xl));
            ProductVariant addVariant = ProductVariant.create("TEST", 1000L, 100, 10);
            addVariant.addProductVariantOptions(List.of(l));
            Product product = ProductTestBuilder.aProduct().withOptions(List.of(size)).build();
            product.addVariant(existVariant);
            //when
            product.addVariant(addVariant);
            //then
            assertThat(product.getVariants()).hasSize(2)
                    .extracting(ProductVariant::getOriginalPrice, ProductVariant::getDiscountRate)
                    .containsExactlyInAnyOrder(
                            tuple(3000L, 40),
                            tuple(1000L, 10)
                    );

            assertThat(product)
                    .extracting(Product::getLowestPrice, Product::getOriginalPrice, Product::getMaxDiscountRate)
                    .containsExactly(900L, 1000L, 40);
        }

        @Test
        @DisplayName("상품 변형이 추가되면 상품의 가격정보[최저 금액, 원본 금액, 최대 할인율]가 최신화 된다")
        void addVariant_price_update(){
            //given
            ProductVariant variant = ProductVariant.create("TEST", 3000L, 100, 10);
            Product product = ProductTestBuilder.aProduct().build();
            //when
            product.addVariant(variant);
            //then
            assertThat(product)
                    .extracting(Product::getLowestPrice, Product::getOriginalPrice, Product::getMaxDiscountRate)
                    .containsExactly(2700L, 3000L, 10);
        }

        @Test
        @DisplayName("삭제된 상품은 상품 변형을 추가할 수 없다")
        void addVariant_product_deleted(){
            //given
            ProductVariant variant = ProductVariant.create("TEST", 3000L, 100, 10);
            Product product = ProductTestBuilder.aProduct().withStatus(ProductStatus.DELETED).build();
            //when
            //then
            assertThatThrownBy(() -> product.addVariant(variant))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
        }

        @Test
        @DisplayName("상품 옵션과 상품 변형 옵션의 개수는 동일해야한다")
        void addVariant_product_option_size_not_match_variant_option_size(){
            //given
            OptionType size = createOptionType(1L, "사이즈", List.of("XL", "L"));
            OptionType color = createOptionType(2L, "색상", List.of("RED", "BLUE"));
            OptionValue xl = getOptionValueSettingId(1L, size, "XL");
            OptionValue blue = getOptionValueSettingId(2L, color, "BLUE");
            ProductVariant variant = ProductVariant.create("TEST", 3000L, 100, 10);
            variant.addProductVariantOptions(List.of(xl, blue));
            Product product = ProductTestBuilder.aProduct().withOptions(List.of(size)).build();
            //when
            //then
            assertThatThrownBy(() -> product.addVariant(variant))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.VARIANT_OPTION_SIZE_MISMATCH);
        }

        @Test
        @DisplayName("상품 변형 옵션은 상품 옵션의 값이여야 한다")
        void addVariant_product_option_not_match_variant_option(){
            //given
            OptionType size = createOptionType(1L, "사이즈", List.of("XL", "L"));
            OptionType color = createOptionType(2L, "색상", List.of("RED", "BLUE"));
            OptionValue blue = getOptionValueSettingId(2L, color, "BLUE");
            ProductVariant variant = ProductVariant.create("TEST", 3000L, 100, 10);
            variant.addProductVariantOptions(List.of(blue));
            Product product = ProductTestBuilder.aProduct().withOptions(List.of(size)).build();
            //when
            //then
            assertThatThrownBy(() -> product.addVariant(variant))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.VARIANT_OPTION_SPEC_MISMATCH);
        }

        @Test
        @DisplayName("상품은 유일한 옵션 조합의 상품 변형만 가질 수 있다")
        void addVariant_product_has_duplicate_variant(){
            //given
            OptionType size = createOptionType(1L, "사이즈", List.of("XL", "L"));
            OptionType color = createOptionType(2L, "색상", List.of("RED", "BLUE"));
            OptionValue xl = getOptionValueSettingId(1L, size, "XL");
            OptionValue blue = getOptionValueSettingId(2L, color, "BLUE");

            ProductVariant existVariant = ProductVariant.create("TEST", 3000L, 100, 10);
            existVariant.addProductVariantOptions(List.of(xl, blue));

            ProductVariant addVariant = ProductVariant.create("TEST", 3000L, 100, 10);
            addVariant.addProductVariantOptions(List.of(blue, xl));

            Product product = ProductTestBuilder.aProduct().withOptions(List.of(size, color)).withVariants(List.of(existVariant)).build();
            //when
            //then
            assertThatThrownBy(() -> product.addVariant(addVariant))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.VARIANT_ALREADY_EXISTS);
        }
    }

    @Nested
    @DisplayName("상품 이미지 추가")
    class AddImage {

        @Test
        @DisplayName("상품 이미지를 추가하면 상품의 대표 이미지가 최신화 된다")
        void replaceImages(){
            //given
            Product product = ProductTestBuilder.aProduct().build();
            //when
            product.replaceImages(List.of("http://thumbnail.jpg", "http://image2.jpg"));
            //then
            assertThat(product.getImages()).hasSize(2)
                    .extracting(ProductImage::getImageUrl, ProductImage::getSortOrder)
                    .containsExactlyInAnyOrder(
                            tuple("http://thumbnail.jpg", 1),
                            tuple("http://image2.jpg", 2)
                    );
            assertThat(product.getThumbnail()).isEqualTo("http://thumbnail.jpg");
        }

        @Test
        @DisplayName("삭제된 상품에 이미지를 추가할 수 없다")
        void replaceImages_product_deleted(){
            //given
            Product product = ProductTestBuilder.aProduct().withStatus(ProductStatus.DELETED).build();
            //when
            //then
            assertThatThrownBy(() -> product.replaceImages(List.of("http://image1.jpg", "http://image2.jpg")))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
        }

        @Test
        @DisplayName("판매중인 상품은 최소 이미지가 한개 이상이여야 한다")
        void replaceImages_product_on_sale(){
            //given
            Product product = ProductTestBuilder.aProduct().withStatus(ProductStatus.ON_SALE).build();
            //when
            //then
            assertThatThrownBy(() -> product.replaceImages(List.of()))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.IMAGE_REQUIRED_ON_SALE);
        }
    }

    @Nested
    @DisplayName("상품 게시")
    class Publish {
        
        @Test
        @DisplayName("상품을 게시한다 [준비중인 상품을 게시]")
        void publish_from_preparing() {
            //given
            ProductVariant variant = ProductVariant.create("TEST", 10000L, 100, 10);
            Product product = ProductTestBuilder.aProduct()
                    .withStatus(ProductStatus.PREPARING)
                    .withVariants(List.of(variant))
                    .withImages(List.of("http://image.jpg"))
                    .withPrice(1000L, 1200L, 10).build();
            //when
            product.publish();
            //then
            assertThat(product.getStatus()).isEqualTo(ProductStatus.ON_SALE);
            assertThat(product.getPublishedAt()).isNotNull();
        }

        @Test
        @DisplayName("상품을 게시한다 [판매 중지가 된 상품을 게시]")
        void publish_from_stop_sale() {
            //given
            ProductVariant variant = ProductVariant.create("TEST", 10000L, 100, 10);
            Product product = ProductTestBuilder.aProduct()
                    .withStatus(ProductStatus.STOP_SALE)
                    .withVariants(List.of(variant))
                    .withImages(List.of("http://image.jpg"))
                    .withPrice(1000L, 1200L, 10).build();
            ReflectionTestUtils.setField(product, "publishedAt", LocalDateTime.of(2026, 1, 1, 10, 10, 20));
            ReflectionTestUtils.setField(product, "saleStoppedAt", LocalDateTime.of(2026, 1, 2, 10, 10, 20));
            //when
            product.publish();
            //then
            assertThat(product.getStatus()).isEqualTo(ProductStatus.ON_SALE);
            assertThat(product.getPublishedAt()).isEqualTo(LocalDateTime.of(2026, 1, 1, 10, 10, 20));
            assertThat(product.getSaleStoppedAt()).isNull();
        }

        @Test
        @DisplayName("삭제된 상품은 게시할 수 없다")
        void publish_product_deleted(){
            //given
            Product product = ProductTestBuilder.aProduct().withStatus(ProductStatus.DELETED).build();
            //when
            //then
            assertThatThrownBy(() -> product.publish())
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.DELETED_PRODUCT_CANNOT_PUBLISH);
        }

        @Test
        @DisplayName("상품 변형이 없는 상품은 게시할 수 없다")
        void publish_product_have_not_variant(){
            //given
            Product product = ProductTestBuilder.aProduct().withVariants(List.of()).build();
            //when
            //then
            assertThatThrownBy(() -> product.publish())
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.VARIANT_REQUIRED_FOR_PUBLISH);
        }

        @Test
        @DisplayName("대표 이미지가 없는 상품은 게시할 수 없다")
        void publish_product_have_not_thumbnail(){
            //given
            ProductVariant variant = ProductVariant.create("TEST", 10000L, 100, 10);
            Product product = ProductTestBuilder.aProduct().withVariants(List.of(variant)).withImages(List.of()).build();
            //when
            //then
            assertThatThrownBy(() -> product.publish())
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.THUMBNAIL_IMAGE_REQUIRED);
        }

        @Test
        @DisplayName("판매가가 유효하지 않으면 게시할 수 없다")
        void publish_product_invalid_lowest_price(){
            //given
            ProductVariant variant = ProductVariant.create("TEST", 10000L, 100, 10);
            Product product = ProductTestBuilder.aProduct()
                    .withVariants(List.of(variant))
                    .withImages(List.of("http://image.jpg"))
                    .withPrice(0L, 0L, 0).build();
            //when
            //then
            assertThatThrownBy(() -> product.publish())
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.DISPLAY_PRICE_INVALID);
        }

        @Test
        @DisplayName("정가가 유효하지 않으면 게시할 수 없다")
        void publish_product_invalid_original_price(){
            //given
            ProductVariant variant = ProductVariant.create("TEST", 10000L, 100, 10);
            Product product = ProductTestBuilder.aProduct()
                    .withVariants(List.of(variant))
                    .withImages(List.of("http://image.jpg"))
                    .withPrice(1000L, 0L, 0).build();
            //when
            //then
            assertThatThrownBy(() -> product.publish())
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.ORIGINAL_PRICE_INVALID);
        }

        @Test
        @DisplayName("할인가가 정가보다 비쌀 수 없다")
        void publish_product_invalid_lowest_greater_than_original_price(){
            //given
            ProductVariant variant = ProductVariant.create("TEST", 10000L, 100, 10);
            Product product = ProductTestBuilder.aProduct()
                    .withVariants(List.of(variant))
                    .withImages(List.of("http://image.jpg"))
                    .withPrice(1000L, 900L, 0).build();
            //when
            //then
            assertThatThrownBy(() -> product.publish())
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.DISPLAY_PRICE_EXCEEDS_ORIGINAL);
        }

        @Test
        @DisplayName("할인율이 유효하지 않으면 게시할 수 없다")
        void publish_product_invalid_discount_rate(){
            //given
            ProductVariant variant = ProductVariant.create("TEST", 10000L, 100, 10);
            Product product = ProductTestBuilder.aProduct()
                    .withVariants(List.of(variant))
                    .withImages(List.of("http://image.jpg"))
                    .withPrice(1000L, 1200L, null).build();
            //when
            //then
            assertThatThrownBy(() -> product.publish())
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.DISCOUNT_RATE_INVALID);
        }
    }

    @Nested
    @DisplayName("상품 수정")
    class Update {

        @Test
        @DisplayName("상품 정보를 수정")
        void updateProductInfo() {
            //given
            Category category = Category.create("카테고리", null, "http://image.jpg");
            Category newCategory = Category.create("새 카테고리", null, "http://image.jpg");
            Product product = ProductTestBuilder.aProduct()
                    .withCategory(category)
                    .withName("상품")
                    .withDescription("상품 설명")
                    .build();
            //when
            product.updateProductInfo("새 이름", "새 설명", newCategory);
            //then
            assertThat(product.getCategory()).isEqualTo(newCategory);
            assertThat(product.getName()).isEqualTo("새 이름");
            assertThat(product.getDescription()).isEqualTo("새 설명");
        }

        @Test
        @DisplayName("상품이 삭제된 상태라면 예외를 던진다")
        void updateProductInfo_deleted_product() {
            //given
            Category category = Category.create("새 카테고리", null, "http://image.jpg");
            Product product = ProductTestBuilder.aProduct()
                    .withStatus(ProductStatus.DELETED)
                    .build();
            //when
            //then
            assertThatThrownBy(() -> product.updateProductInfo("새 이름", "새 설명", category))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
        }

        @Test
        @DisplayName("변경할 상품 카테고리가 없으면 예외를 던진다")
        void updateProductInfo_category_null() {
            //given
            Product product = ProductTestBuilder.aProduct().build();
            //when
            //then
            assertThatThrownBy(() -> product.updateProductInfo("새 이름", "새 설명", null))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.PRODUCT_CATEGORY_REQUIRED);
        }

        @Test
        @DisplayName("변경할 상품 카테고리가 최하위 카테고리가 아니면 예외를 던진다")
        void updateProductInfo_parent_category() {
            //given
            Category parent = Category.create("부모 카테고리", null, "http://image.jpg");
            Category child = Category.create("자식 카테고리", parent, "http://image.jpg");
            Product product = ProductTestBuilder.aProduct().build();
            //when
            //then
            assertThatThrownBy(() -> product.updateProductInfo("새 상품", "새 설명", parent))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.CATEGORY_NOT_LEAF);
        }
    }

    @Nested
    @DisplayName("상품 삭제")
    class Delete {
        @Test
        @DisplayName("상품을 삭제한다")
        void deleted() {
            //given
            Product product = ProductTestBuilder.aProduct().build();
            //when
            product.deleted();
            //then
            assertThat(product.getStatus()).isEqualTo(ProductStatus.DELETED);
        }
    }

    @Nested
    @DisplayName("상품 판매 중지")
    class Close {
        @Test
        @DisplayName("상품을 판매 중지 한다")
        void closed() {
            //given
            Product product = ProductTestBuilder.aProduct().withStatus(ProductStatus.ON_SALE).build();
            //when
            product.closed();
            //then
            assertThat(product.getStatus()).isEqualTo(ProductStatus.STOP_SALE);
            assertThat(product.getSaleStoppedAt()).isNotNull();
        }

        @Test
        @DisplayName("판매중인 상품이 아니면 판매 중지 할 수 없다")
        void closed_not_on_sale() {
            //given
            Product product = ProductTestBuilder.aProduct().withStatus(ProductStatus.PREPARING).build();
            //when
            //then
            assertThatThrownBy(() -> product.closed())
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.INVALID_STATUS_FOR_STOP_SALE);
        }
    }
}
