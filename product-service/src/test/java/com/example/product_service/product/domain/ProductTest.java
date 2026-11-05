package com.example.product_service.product.domain;

import com.example.product_service.common.domain.vo.Money;
import com.example.product_service.common.exception.BusinessException;
import com.example.product_service.product.domain.context.AddVariantContext;
import com.example.product_service.product.domain.context.CreateProductContext;
import com.example.product_service.product.domain.context.RegisterOptionTypeContext;
import com.example.product_service.product.domain.context.UpdateProductContext;
import com.example.product_service.product.domain.vo.SalePrice;
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
        Product product = aProduct();
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
        Product product = aProduct();
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
        Product product = aProduct();
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
    @DisplayName("삭제된 상품은 수정할 수 없다")
    void update_whenProductDeleted_thenThrownException() {
        //given
        Product product = aProduct();
        product.deleted(LocalDateTime.now());

        UpdateProductContext context = UpdateProductContext.builder()
                .name("변경된 상품")
                .categoryId(20L)
                .description("변경된 상품 설명")
                .build();
        //when
        //then
        assertThatThrownBy(() -> product.update(context))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ProductErrorCode.CANNOT_UPDATE);
    }

    @Test
    @DisplayName("상품 옵션 타입을 등록한다")
    void registerOptionTypes() {
        //given
        Product product = aProduct();
        List<RegisterOptionTypeContext> contexts = List.of(
                RegisterOptionTypeContext.builder().id(1L).optionTypeId(10L).build(),
                RegisterOptionTypeContext.builder().id(2L).optionTypeId(20L).build()
        );
        //when
        product.registerOptionTypes(contexts, LocalDateTime.now());
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
        Product product = aProduct();
        product.deleted(LocalDateTime.now());

        List<RegisterOptionTypeContext> contexts = List.of(
                RegisterOptionTypeContext.builder().id(1L).optionTypeId(10L).build()
        );
        //when
        //then
        assertThatThrownBy(() -> product.registerOptionTypes(contexts, LocalDateTime.now()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ProductErrorCode.CANNOT_REGISTER_OPTION_TYPE);
    }

    @Test
    @DisplayName("옵션 타입은 최대 3개까지만 등록할 수 있다")
    void registerOptionTypes_whenExceedMaxSize_thenThrownException() {
        //given
        Product product = aProduct();
        List<RegisterOptionTypeContext> contexts = List.of(
                RegisterOptionTypeContext.builder().id(1L).optionTypeId(10L).build(),
                RegisterOptionTypeContext.builder().id(2L).optionTypeId(20L).build(),
                RegisterOptionTypeContext.builder().id(3L).optionTypeId(30L).build(),
                RegisterOptionTypeContext.builder().id(4L).optionTypeId(40L).build()
        );
        //when
        //then
        assertThatThrownBy(() -> product.registerOptionTypes(contexts, LocalDateTime.now()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ProductErrorCode.EXCEED_MAX_OPTION_SIZE);
    }

    @Test
    @DisplayName("중복된 옵션 타입은 등록할 수 없다")
    void registerOptionTypes_whenOptionTypeDuplicated_thenThrownException() {
        //given
        Product product = aProduct();
        List<RegisterOptionTypeContext> contexts = List.of(
                RegisterOptionTypeContext.builder().id(1L).optionTypeId(10L).build(),
                RegisterOptionTypeContext.builder().id(2L).optionTypeId(10L).build()
        );
        //when
        //then
        assertThatThrownBy(() -> product.registerOptionTypes(contexts, LocalDateTime.now()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ProductErrorCode.OPTION_TYPE_DUPLICATED);
    }

    @Test
    @DisplayName("옵션 타입을 다시 등록하면 기존 상품 변형은 판매 중지된다")
    void registerOptionTypes_discontinuesExistingVariants() {
        //given
        Product product = aProduct();
        product.registerOptionTypes(
                List.of(RegisterOptionTypeContext.builder().id(1L).optionTypeId(10L).build()),
                LocalDateTime.now()
        );
        product.addVariants(List.of(aVariantContext(1L, "SKU-001", 10L, 100L)));

        LocalDateTime registeredAt = LocalDateTime.now();
        //when
        product.registerOptionTypes(
                List.of(RegisterOptionTypeContext.builder().id(2L).optionTypeId(20L).build()),
                registeredAt
        );
        //then
        ProductVariant variant = product.getVariants().get(0);
        assertThat(variant.getStatus()).isEqualTo(ProductVariantStatus.DISCONTINUED);
        assertThat(variant.getDiscontinuedAt()).isEqualTo(registeredAt);
    }

    @Test
    @DisplayName("상품을 삭제한다")
    void deleted() {
        //given
        Product product = aProduct();
        LocalDateTime deletedAt = LocalDateTime.now();
        //when
        product.deleted(deletedAt);
        //then
        assertThat(product.getStatus()).isEqualTo(ProductStatus.DELETED);
        assertThat(product.getDeletedAt()).isEqualTo(deletedAt);
    }

    @Test
    @DisplayName("이미 삭제된 상품은 다시 삭제할 수 없다")
    void deleted_whenAlreadyDeleted_thenThrownException() {
        //given
        Product product = aProduct();
        product.deleted(LocalDateTime.now());
        //when
        //then
        assertThatThrownBy(() -> product.deleted(LocalDateTime.now()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ProductErrorCode.ALREADY_DELETED);
    }

    @Test
    @DisplayName("상품을 삭제하면 기존 상품 변형은 판매 중지된다")
    void deleted_discontinuesExistingVariants() {
        //given
        Product product = aProduct();
        product.registerOptionTypes(
                List.of(RegisterOptionTypeContext.builder().id(1L).optionTypeId(10L).build()),
                LocalDateTime.now()
        );
        product.addVariants(List.of(aVariantContext(1L, "SKU-001", 10L, 100L)));

        LocalDateTime deletedAt = LocalDateTime.now();
        //when
        product.deleted(deletedAt);
        //then
        ProductVariant variant = product.getVariants().get(0);
        assertThat(variant.getStatus()).isEqualTo(ProductVariantStatus.DISCONTINUED);
        assertThat(variant.getDiscontinuedAt()).isEqualTo(deletedAt);
    }

    @Test
    @DisplayName("상품 변형을 추가한다")
    void addVariants() {
        //given
        Product product = aProduct();
        product.registerOptionTypes(
                List.of(RegisterOptionTypeContext.builder().id(1L).optionTypeId(10L).build()),
                LocalDateTime.now()
        );
        AddVariantContext context = aVariantContext(1L, "SKU-001", 10L, 100L);
        //when
        product.addVariants(List.of(context));
        //then
        assertThat(product.getVariants()).hasSize(1);
        ProductVariant variant = product.getVariants().get(0);
        assertThat(variant.getSku()).isEqualTo("SKU-001");
        assertThat(variant.getStatus()).isEqualTo(ProductVariantStatus.PREPARING);
        assertThat(variant.getProductVariantOptionValues())
                .extracting("optionValueId")
                .containsExactly(100L);
    }

    @Test
    @DisplayName("삭제된 상품에는 상품 변형을 추가할 수 없다")
    void addVariants_whenProductDeleted_thenThrownException() {
        //given
        Product product = aProduct();
        product.deleted(LocalDateTime.now());
        AddVariantContext context = aVariantContext(1L, "SKU-001", 10L, 100L);
        //when
        //then
        assertThatThrownBy(() -> product.addVariants(List.of(context)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ProductErrorCode.CANNOT_ADD_VARIANT);
    }

    @Test
    @DisplayName("등록된 옵션 타입 개수와 옵션 값 개수가 다르면 예외가 발생한다")
    void addVariants_whenOptionValueCountMismatch_thenThrownException() {
        //given
        Product product = aProduct();
        product.registerOptionTypes(
                List.of(
                        RegisterOptionTypeContext.builder().id(1L).optionTypeId(10L).build(),
                        RegisterOptionTypeContext.builder().id(2L).optionTypeId(20L).build()
                ),
                LocalDateTime.now()
        );
        AddVariantContext context = aVariantContext(1L, "SKU-001", 10L, 100L);
        //when
        //then
        assertThatThrownBy(() -> product.addVariants(List.of(context)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ProductErrorCode.INVALID_OPTION_VALUE_COUNT);
    }

    @Test
    @DisplayName("등록된 옵션 타입과 일치하지 않는 옵션 값이면 예외가 발생한다")
    void addVariants_whenOptionTypeMismatch_thenThrownException() {
        //given
        Product product = aProduct();
        product.registerOptionTypes(
                List.of(RegisterOptionTypeContext.builder().id(1L).optionTypeId(10L).build()),
                LocalDateTime.now()
        );
        AddVariantContext context = aVariantContext(1L, "SKU-001", 99L, 100L);
        //when
        //then
        assertThatThrownBy(() -> product.addVariants(List.of(context)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ProductErrorCode.VARIANT_OPTION_MISMATCH);
    }

    @Test
    @DisplayName("동일한 옵션 값 조합의 상품 변형은 중복 추가할 수 없다")
    void addVariants_whenDuplicateOptionCombination_thenThrownException() {
        //given
        Product product = aProduct();
        product.registerOptionTypes(
                List.of(RegisterOptionTypeContext.builder().id(1L).optionTypeId(10L).build()),
                LocalDateTime.now()
        );
        product.addVariants(List.of(aVariantContext(1L, "SKU-001", 10L, 100L)));
        //when
        //then
        assertThatThrownBy(() -> product.addVariants(List.of(aVariantContext(2L, "SKU-002", 10L, 100L))))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ProductErrorCode.VARIANT_DUPLICATE_OPTION);
    }

    @Test
    @DisplayName("상품 변형의 재고가 0보다 작으면 예외가 발생한다")
    void addVariants_whenStockIsNegative_thenThrownException() {
        //given
        Product product = aProduct();
        product.registerOptionTypes(
                List.of(RegisterOptionTypeContext.builder().id(1L).optionTypeId(10L).build()),
                LocalDateTime.now()
        );
        AddVariantContext context = AddVariantContext.builder()
                .id(1L)
                .sku("SKU-001")
                .salePrice(aSalePrice())
                .stock(-1)
                .optionValues(List.of(
                        AddVariantContext.AddVariantOptionValueContext.builder()
                                .id(1L)
                                .optionTypeId(10L)
                                .optionValueId(100L)
                                .build()
                ))
                .build();
        //when
        //then
        assertThatThrownBy(() -> product.addVariants(List.of(context)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ProductErrorCode.VARIANT_INVALID_STOCK);
    }

    private Product aProduct() {
        return Product.create(
                CreateProductContext.builder()
                        .id(1L)
                        .categoryId(10L)
                        .name("상품")
                        .description("상품 설명")
                        .build()
        );
    }

    private AddVariantContext aVariantContext(Long id, String sku, Long optionTypeId, Long optionValueId) {
        return AddVariantContext.builder()
                .id(id)
                .sku(sku)
                .salePrice(aSalePrice())
                .stock(100)
                .optionValues(List.of(
                        AddVariantContext.AddVariantOptionValueContext.builder()
                                .id(id)
                                .optionTypeId(optionTypeId)
                                .optionValueId(optionValueId)
                                .build()
                ))
                .build();
    }

    private SalePrice aSalePrice() {
        return SalePrice.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
    }
}
