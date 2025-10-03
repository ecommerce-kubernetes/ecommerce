package com.example.product_service.service.unit;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.request.variant.UpdateProductVariantRequest;
import com.example.product_service.dto.response.variant.ProductVariantResponse;
import com.example.product_service.entity.*;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.exception.InsufficientStockException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.ProductVariantsRepository;
import com.example.product_service.service.ProductVariantService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static com.example.product_service.common.MessagePath.PRODUCT_VARIANT_NOT_FOUND;
import static com.example.product_service.util.TestMessageUtil.getMessage;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductVariantServiceUnitTest {

    @Mock
    ProductVariantsRepository productVariantsRepository;
    @Mock
    MessageSourceUtil ms;

    @InjectMocks
    ProductVariantService productVariantService;

    @Test
    @DisplayName("상품 변형 수정 테스트-성공")
    void updateVariantByIdTest_unit_success(){
        OptionType optionType = new OptionType("optionType");
        OptionValue optionValue = new OptionValue("optionValue");
        optionType.addOptionValue(optionValue);

        ProductOptionType productOptionType = createProductOptionTypes(optionType);
        ProductVariantOption productVariantOption = new ProductVariantOption(optionValue);
        ProductVariant productVariant = createProductVariant(1L,"sku", List.of(productVariantOption));

        Product product = createProduct(
                List.of(new ProductImage("http://test.jpg", 0)),
                List.of(productOptionType),
                List.of(productVariant));

        when(productVariantsRepository.findWithProductById(1L))
                .thenReturn(Optional.of(productVariant));

        UpdateProductVariantRequest request = new UpdateProductVariantRequest(50000, 10, 20);
        ProductVariantResponse response = productVariantService.updateVariantById(1L, request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getSku()).isEqualTo("sku");
        assertThat(response.getPrice()).isEqualTo(50000);
        assertThat(response.getStockQuantity()).isEqualTo(10);
        assertThat(response.getDiscountRate()).isEqualTo(20);
    }

    @Test
    @DisplayName("상품 변형 수정 테스트-실패(상품 변형을 찾을 수 없음)")
    void updateVariantByIdTest_unit_notFound_productVariant(){
        when(productVariantsRepository.findWithProductById(1L))
                .thenReturn(Optional.empty());
        when(ms.getMessage(PRODUCT_VARIANT_NOT_FOUND))
                .thenReturn("Product Variant not found");

        UpdateProductVariantRequest request = new UpdateProductVariantRequest(50000, 10, 20);
        assertThatThrownBy(() -> productVariantService.updateVariantById(1L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(PRODUCT_VARIANT_NOT_FOUND));
    }

    @Test
    @DisplayName("상품 변형 삭제 테스트-성공")
    void deleteVariantByIdTest_unit_success(){
        OptionType optionType = new OptionType("optionType");
        OptionValue optionValue1 = createOptionValue(1L, "optionValue1", optionType);
        OptionValue optionValue2 = createOptionValue(2L, "optionValue2", optionType);

        ProductOptionType productOptionType = createProductOptionTypes(optionType);
        ProductVariantOption productVariantOption1 = new ProductVariantOption(optionValue1);
        ProductVariantOption productVariantOption2 = new ProductVariantOption(optionValue2);
        ProductVariant productVariant1 = createProductVariant(1L, "sku1", List.of(productVariantOption1));
        ProductVariant productVariant2 = createProductVariant(2L, "sku2", List.of(productVariantOption2));

        Product product = createProduct(
                List.of(new ProductImage("http://test.jpg", 0)),
                List.of(productOptionType),
                List.of(productVariant1, productVariant2));
        when(productVariantsRepository.findWithProductById(1L))
                .thenReturn(Optional.of(productVariant1));

        productVariantService.deleteVariantById(1L);

        assertThat(product.getProductVariants().size()).isEqualTo(1);
        assertThat(product.getProductVariants())
                .extracting(ProductVariant::getId, ProductVariant::getSku, ProductVariant::getPrice,
                        ProductVariant::getStockQuantity, ProductVariant::getDiscountValue)
                .containsExactlyInAnyOrder(
                        tuple(2L, "sku2", 10000, 100, 10)
                );

        assertThat(product.getProductVariants())
                .flatExtracting(ProductVariant::getProductVariantOptions)
                .extracting(pvo -> pvo.getOptionValue().getOptionValue())
                .containsExactlyInAnyOrder( "optionValue2");
    }

    @Test
    @DisplayName("상품 변형 삭제 테스트-실패(상품 변형을 찾을 수 없음)")
    void deleteVariantByIdTest_unit_notFound_productVariant(){
        when(productVariantsRepository.findWithProductById(1L))
                .thenReturn(Optional.empty());
        when(ms.getMessage(PRODUCT_VARIANT_NOT_FOUND)).thenReturn("Product Variant not found");

        assertThatThrownBy(() -> productVariantService.deleteVariantById(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(PRODUCT_VARIANT_NOT_FOUND));
    }

    @Test
    @DisplayName("상품 변형 삭제 테스트-실패(상품에 존재하는 상품 변형이 1개일때)")
    void deleteVariantByIdTest_unit_variant_minimum_number(){
        OptionType optionType = new OptionType("optionType");
        OptionValue optionValue = new OptionValue("optionValue");
        optionType.addOptionValue(optionValue);

        ProductOptionType productOptionType = createProductOptionTypes(optionType);
        ProductVariantOption productVariantOption = new ProductVariantOption(optionValue);
        ProductVariant productVariant = createProductVariant(1L,"sku", List.of(productVariantOption));

        Product product = createProduct(
                List.of(new ProductImage("http://test.jpg", 0)),
                List.of(productOptionType),
                List.of(productVariant));
        when(productVariantsRepository.findWithProductById(1L))
                .thenReturn(Optional.of(productVariant));

        assertThatThrownBy(() -> productVariantService.deleteVariantById(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("must be at least one product variant per product");
    }

    @Test
    @DisplayName("재고 감소-성공")
    void inventoryReductionByIdTest_unit_success(){
        OptionType optionType = new OptionType("optionType");
        OptionValue optionValue = new OptionValue("optionValue");
        optionType.addOptionValue(optionValue);

        ProductOptionType productOptionType = createProductOptionTypes(optionType);
        ProductVariantOption productVariantOption = new ProductVariantOption(optionValue);
        ProductVariant productVariant = createProductVariant(1L,"sku", List.of(productVariantOption));
        Product product = createProduct(
                List.of(new ProductImage("http://test.jpg", 0)),
                List.of(productOptionType),
                List.of(productVariant));

        when(productVariantsRepository.findByIdIn(Set.of(1L)))
                .thenReturn(List.of(productVariant));

        Map<Long, Integer> reductionMap = new HashMap<>();
        reductionMap.put(1L, 10);

        Map<Long, Integer> resultMap = productVariantService.inventoryReductionById(reductionMap);

        assertThat(resultMap.keySet()).contains(1L);
        assertThat(resultMap.values()).contains(10);
        assertThat(productVariant.getStockQuantity()).isEqualTo(90);
    }

    @Test
    @DisplayName("재고 감소-실패(상품 변형을 찾을 수 없음)")
    void inventoryReductionByIdTest_unit_notFound_productVariant(){
        when(productVariantsRepository.findByIdIn(Set.of(1L)))
                .thenReturn(List.of());
        when(ms.getMessage(PRODUCT_VARIANT_NOT_FOUND))
                .thenReturn("Product Variant not found");

        Map<Long, Integer> reductionMap = new HashMap<>();
        reductionMap.put(1L, 10);

        assertThatThrownBy(() -> productVariantService.inventoryReductionById(reductionMap))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(PRODUCT_VARIANT_NOT_FOUND));
    }

    @Test
    @DisplayName("재고 감소-실패(재고가 부족함)")
    void inventoryReductionByIdTest_unit_outOfStock(){
        OptionType optionType = new OptionType("optionType");
        OptionValue optionValue = new OptionValue("optionValue");
        optionType.addOptionValue(optionValue);

        ProductOptionType productOptionType = createProductOptionTypes(optionType);
        ProductVariantOption productVariantOption = new ProductVariantOption(optionValue);
        ProductVariant productVariant = createProductVariant(1L,"sku", List.of(productVariantOption));
        productVariant.setStockQuantity(5);
        Product product = createProduct(
                List.of(new ProductImage("http://test.jpg", 0)),
                List.of(productOptionType),
                List.of(productVariant));

        when(productVariantsRepository.findByIdIn(Set.of(1L)))
                .thenReturn(List.of(productVariant));

        Map<Long, Integer> reductionMap = new HashMap<>();
        reductionMap.put(1L, 10);

        assertThatThrownBy(() -> productVariantService.inventoryReductionById(reductionMap))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessage("Out of Stock");
    }

    @Test
    @DisplayName("재고 복원-성공")
    void inventoryRestorationByIdTest_unit_success(){
        OptionType optionType = new OptionType("optionType");
        OptionValue optionValue = new OptionValue("optionValue");
        optionType.addOptionValue(optionValue);

        ProductOptionType productOptionType = createProductOptionTypes(optionType);
        ProductVariantOption productVariantOption = new ProductVariantOption(optionValue);
        ProductVariant productVariant = createProductVariant(1L,"sku", List.of(productVariantOption));
        productVariant.setStockQuantity(90);
        Product product = createProduct(
                List.of(new ProductImage("http://test.jpg", 0)),
                List.of(productOptionType),
                List.of(productVariant));

        when(productVariantsRepository.findByIdIn(Set.of(1L)))
                .thenReturn(List.of(productVariant));

        Map<Long, Integer> restoreMap = new HashMap<>();
        restoreMap.put(1L, 10);

        productVariantService.inventoryRestorationById(restoreMap);

        assertThat(productVariant.getStockQuantity()).isEqualTo(100);
    }

    @Test
    @DisplayName("재고 복원-실패(상품 변형을 찾을 수 없음)")
    void inventoryRestorationByIdTest_unit_notFound_productVariant(){
        when(productVariantsRepository.findByIdIn(Set.of(1L)))
                .thenReturn(List.of());

        when(ms.getMessage(PRODUCT_VARIANT_NOT_FOUND))
                .thenReturn("Product Variant not found");

        Map<Long, Integer> restoreMap = new HashMap<>();
        restoreMap.put(1L, 10);

        assertThatThrownBy(() -> productVariantService.inventoryRestorationById(restoreMap))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(PRODUCT_VARIANT_NOT_FOUND));
    }

    private Product createProduct(List<ProductImage> productImages, List<ProductOptionType> productOptionTypes,
                                  List<ProductVariant> productVariants){
        Product product = new Product("productName", "product description",
                new Category("category", "http://test.jpg"));

        product.addImages(productImages);
        product.addOptionTypes(productOptionTypes);
        product.addVariants(productVariants);
        return product;
    }

    private ProductOptionType createProductOptionTypes(OptionType optionType){
        return new ProductOptionType(optionType, 0, true);
    }

    private ProductVariant createProductVariant(Long variantId, String sku, List<ProductVariantOption> productVariantOptions){
        ProductVariant productVariant = new ProductVariant(sku, 10000, 100, 10);
        productVariant.addProductVariantOptions(productVariantOptions);
        ReflectionTestUtils.setField(productVariant, "id", variantId);
        return productVariant;
    }

    private OptionValue createOptionValue(Long id, String name, OptionType optionType){
        OptionValue optionValue = new OptionValue(name);
        ReflectionTestUtils.setField(optionValue, "id", id);
        optionType.addOptionValue(optionValue);
        return optionValue;
    }
}
