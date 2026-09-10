package com.example.product_service.product.application.service;

import com.example.product_service.common.domain.vo.Money;
import com.example.product_service.common.exception.BusinessException;
import com.example.product_service.product.adapter.out.util.SkuGenerator;
import com.example.product_service.product.domain.context.RegisterOptionTypeContext;
import com.example.product_service.product.exception.ProductErrorCode;
import com.example.product_service.common.util.IdGenerator;
import com.example.product_service.product.application.port.ProductCategoryPort;
import com.example.product_service.product.application.port.ProductOptionPort;
import com.example.product_service.product.application.port.ProductRepository;
import com.example.product_service.product.application.port.dto.ProductCategoryResult;
import com.example.product_service.product.application.port.dto.ProductOptionTypesResult;
import com.example.product_service.product.application.port.dto.ProductOptionValuesResult;
import com.example.product_service.product.application.service.dto.command.AddProductVariantCommand;
import com.example.product_service.product.application.service.dto.command.CreateProductCommand;
import com.example.product_service.product.application.service.dto.command.RegisterProductOptionCommand;
import com.example.product_service.product.application.service.dto.command.UpdateProductCommand;
import com.example.product_service.product.domain.Product;
import com.example.product_service.product.fixture.ProductFixtureBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static com.example.product_service.product.fixture.ProductCommandFixture.anCreateProductCommand;
import static com.example.product_service.product.fixture.ProductCommandFixture.anUpdateProductCommand;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ProductCommandServiceTest {

    @InjectMocks
    private ProductCommandService productCommandService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductCategoryPort productCategoryPort;

    @Mock
    private ProductOptionPort productOptionPort;

    @Mock
    private IdGenerator idGenerator;

    @Mock
    private SkuGenerator skuGenerator;

    @Mock
    private Clock clock;

    @Captor
    private ArgumentCaptor<Product> productCaptor;

    @Test
    @DisplayName("상품을 생성한다")
    void createProduct() {
        //given
        Long expectedId = 100L;
        CreateProductCommand command = anCreateProductCommand().build();
        ProductCategoryResult category = new ProductCategoryResult(command.categoryId(), "카테고리", true);

        given(productCategoryPort.getCategory(command.categoryId())).willReturn(category);
        given(idGenerator.generate()).willReturn(expectedId);
        given(productRepository.save(any(Product.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        //when
        Long productId = productCommandService.createProduct(command);
        //then
        assertThat(productId).isEqualTo(expectedId);

        then(productRepository).should().save(productCaptor.capture());
        Product savedProduct = productCaptor.getValue();
        assertThat(savedProduct.getId()).isEqualTo(expectedId);
        assertThat(savedProduct.getName()).isEqualTo(command.name());
        assertThat(savedProduct.getCategoryId()).isEqualTo(command.categoryId());
        assertThat(savedProduct.getDescription()).isEqualTo(command.description());
    }

    @Test
    @DisplayName("카테고리가 리프 카테고리가 아니면 예외가 발생하고 상품을 생성하지 않는다")
    void createProduct_whenCategoryNotLeaf_thenThrownException() {
        //given
        CreateProductCommand command = anCreateProductCommand().build();
        ProductCategoryResult category = new ProductCategoryResult(command.categoryId(), "카테고리", false);

        given(productCategoryPort.getCategory(command.categoryId())).willReturn(category);
        //when
        //then
        assertThatThrownBy(() -> productCommandService.createProduct(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ProductErrorCode.CATEGORY_NOT_LEAF);

        then(productRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("상품 정보를 수정한다")
    void updateProduct() {
        //given
        Product product = ProductFixtureBuilder.given().withName("상품").build();
        UpdateProductCommand command = anUpdateProductCommand().productId(product.getId()).build();
        ProductCategoryResult category = new ProductCategoryResult(command.categoryId(), "카테고리", true);

        given(productRepository.findById(product.getId())).willReturn(Optional.of(product));
        given(productCategoryPort.getCategory(command.categoryId())).willReturn(category);
        //when
        Long productId = productCommandService.updateProduct(command);
        //then
        assertThat(productId).isEqualTo(product.getId());
        assertThat(product.getName()).isEqualTo(command.name());
        assertThat(product.getCategoryId()).isEqualTo(command.categoryId());
        assertThat(product.getDescription()).isEqualTo(command.description());
    }

    @Test
    @DisplayName("수정할 상품을 찾을 수 없으면 예외가 발생한다")
    void updateProduct_whenProductNotFound_thenThrownException() {
        //given
        UpdateProductCommand command = anUpdateProductCommand().productId(999L).build();
        given(productRepository.findById(999L)).willReturn(Optional.empty());
        //when
        //then
        assertThatThrownBy(() -> productCommandService.updateProduct(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("수정할 카테고리가 리프 카테고리가 아니면 예외가 발생하고 수정하지 않는다")
    void updateProduct_whenCategoryNotLeaf_thenThrownException() {
        //given
        Product product = ProductFixtureBuilder.given().withName("상품").build();
        UpdateProductCommand command = anUpdateProductCommand().productId(product.getId()).build();
        ProductCategoryResult category = new ProductCategoryResult(command.categoryId(), "카테고리", false);

        given(productRepository.findById(product.getId())).willReturn(Optional.of(product));
        given(productCategoryPort.getCategory(command.categoryId())).willReturn(category);
        //when
        //then
        assertThatThrownBy(() -> productCommandService.updateProduct(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ProductErrorCode.CATEGORY_NOT_LEAF);

        assertThat(product.getName()).isEqualTo("상품");
    }

    @Test
    @DisplayName("상품에 옵션 타입을 등록한다")
    void registerProductOptions() {
        //given
        Product product = ProductFixtureBuilder.given().build();
        RegisterProductOptionCommand command = RegisterProductOptionCommand.builder()
                .productId(product.getId())
                .optionTypeIds(List.of(10L, 20L))
                .build();
        ProductOptionTypesResult optionTypes = ProductOptionTypesResult.builder()
                .optionTypes(List.of(
                        new ProductOptionTypesResult.ProductOptionTypeResult(10L, "색상"),
                        new ProductOptionTypesResult.ProductOptionTypeResult(20L, "사이즈")
                ))
                .build();

        given(productRepository.findById(product.getId())).willReturn(Optional.of(product));
        given(productOptionPort.getOptionTypes(command.optionTypeIds())).willReturn(optionTypes);
        given(idGenerator.generate()).willReturn(1L, 2L);
        given(clock.instant()).willReturn(Instant.now());
        given(clock.getZone()).willReturn(ZoneId.systemDefault());
        //when
        Long productId = productCommandService.registerProductOptions(command);
        //then
        assertThat(productId).isEqualTo(product.getId());
        assertThat(product.getProductOptionTypes()).hasSize(2);
        assertThat(product.getProductOptionTypes())
                .extracting("optionTypeId")
                .containsExactly(10L, 20L);
    }

    @Test
    @DisplayName("옵션 타입을 등록할 상품을 찾을 수 없으면 예외가 발생한다")
    void registerProductOptions_whenProductNotFound_thenThrownException() {
        //given
        RegisterProductOptionCommand command = RegisterProductOptionCommand.builder()
                .productId(999L)
                .optionTypeIds(List.of(10L))
                .build();
        given(productRepository.findById(999L)).willReturn(Optional.empty());
        //when
        //then
        assertThatThrownBy(() -> productCommandService.registerProductOptions(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("옵션 타입을 4개 이상 등록하면 예외가 발생한다")
    void registerProductOptions_whenExceedMaxOptionSize_thenThrownException() {
        //given
        Product product = ProductFixtureBuilder.given().build();
        RegisterProductOptionCommand command = RegisterProductOptionCommand.builder()
                .productId(product.getId())
                .optionTypeIds(List.of(10L, 20L, 30L, 40L))
                .build();
        ProductOptionTypesResult optionTypes = ProductOptionTypesResult.builder()
                .optionTypes(List.of(
                        new ProductOptionTypesResult.ProductOptionTypeResult(10L, "색상"),
                        new ProductOptionTypesResult.ProductOptionTypeResult(20L, "사이즈"),
                        new ProductOptionTypesResult.ProductOptionTypeResult(30L, "재질"),
                        new ProductOptionTypesResult.ProductOptionTypeResult(40L, "무게")
                ))
                .build();

        given(productRepository.findById(product.getId())).willReturn(Optional.of(product));
        given(productOptionPort.getOptionTypes(command.optionTypeIds())).willReturn(optionTypes);
        given(idGenerator.generate()).willReturn(1L, 2L, 3L, 4L);
        given(clock.instant()).willReturn(Instant.now());
        given(clock.getZone()).willReturn(ZoneId.systemDefault());
        //when
        //then
        assertThatThrownBy(() -> productCommandService.registerProductOptions(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ProductErrorCode.EXCEED_MAX_OPTION_SIZE);
    }

    @Test
    @DisplayName("상품 변형을 추가한다")
    void addProductVariants() {
        //given
        Product product = ProductFixtureBuilder.given().build();
        product.registerOptionTypes(
                List.of(RegisterOptionTypeContext.builder().id(1L).optionTypeId(10L).build()),
                LocalDateTime.now()
        );

        AddProductVariantCommand command = AddProductVariantCommand.builder()
                .productId(product.getId())
                .variants(List.of(
                        AddProductVariantCommand.VariantDetail.builder()
                                .originalPrice(10000L)
                                .discountRate(10)
                                .stockQuantity(100)
                                .optionValueIds(List.of(100L))
                                .build()
                ))
                .build();
        ProductOptionValuesResult optionValues = ProductOptionValuesResult.builder()
                .optionValues(List.of(new ProductOptionValuesResult.OptionValueResult(100L, 10L, "빨강")))
                .build();

        given(productRepository.findById(product.getId())).willReturn(Optional.of(product));
        given(productOptionPort.getOptionValues(List.of(100L))).willReturn(optionValues);
        given(skuGenerator.generate()).willReturn("SKU-001");
        given(idGenerator.generate()).willReturn(1L, 2L);
        //when
        Long productId = productCommandService.addProductVariants(command);
        //then
        assertThat(productId).isEqualTo(product.getId());
        assertThat(product.getVariants()).hasSize(1);
        assertThat(product.getVariants().get(0).getSku()).isEqualTo("SKU-001");
        assertThat(product.getVariants().get(0).getStock()).isEqualTo(100);
        assertThat(product.getVariants().get(0).getSalePrice().getOriginalPrice()).isEqualTo(Money.wons(10000L));
    }

    @Test
    @DisplayName("상품 변형을 추가할 상품을 찾을 수 없으면 예외가 발생한다")
    void addProductVariants_whenProductNotFound_thenThrownException() {
        //given
        AddProductVariantCommand command = AddProductVariantCommand.builder()
                .productId(999L)
                .variants(List.of())
                .build();
        given(productRepository.findById(999L)).willReturn(Optional.empty());
        //when
        //then
        assertThatThrownBy(() -> productCommandService.addProductVariants(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("등록된 옵션과 일치하지 않는 옵션 값으로 변형을 추가하면 예외가 발생하고 추가하지 않는다")
    void addProductVariants_whenOptionMismatch_thenThrownException() {
        //given
        Product product = ProductFixtureBuilder.given().build();
        product.registerOptionTypes(
                List.of(RegisterOptionTypeContext.builder().id(1L).optionTypeId(10L).build()),
                LocalDateTime.now()
        );

        AddProductVariantCommand command = AddProductVariantCommand.builder()
                .productId(product.getId())
                .variants(List.of(
                        AddProductVariantCommand.VariantDetail.builder()
                                .originalPrice(10000L)
                                .discountRate(10)
                                .stockQuantity(100)
                                .optionValueIds(List.of(200L))
                                .build()
                ))
                .build();
        ProductOptionValuesResult optionValues = ProductOptionValuesResult.builder()
                .optionValues(List.of(new ProductOptionValuesResult.OptionValueResult(200L, 20L, "블루")))
                .build();

        given(productRepository.findById(product.getId())).willReturn(Optional.of(product));
        given(productOptionPort.getOptionValues(List.of(200L))).willReturn(optionValues);
        given(skuGenerator.generate()).willReturn("SKU-001");
        given(idGenerator.generate()).willReturn(1L, 2L);
        //when
        //then
        assertThatThrownBy(() -> productCommandService.addProductVariants(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ProductErrorCode.VARIANT_OPTION_MISMATCH);

        assertThat(product.getVariants()).isEmpty();
    }
}
