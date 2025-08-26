package com.example.product_service.service.unit;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.request.variant.UpdateProductVariantRequest;
import com.example.product_service.dto.response.variant.ProductVariantResponse;
import com.example.product_service.entity.*;
import com.example.product_service.exception.BadRequestException;
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

import java.util.List;
import java.util.Optional;

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
        OptionTypes optionTypes = new OptionTypes("optionType");
        OptionValues optionValues = new OptionValues("optionValue");
        optionTypes.addOptionValue(optionValues);

        ProductOptionTypes productOptionTypes = createProductOptionTypes(optionTypes);
        ProductVariantOptions productVariantOption = new ProductVariantOptions(optionValues);
        ProductVariants productVariant = createProductVariant(1L,"sku", List.of(productVariantOption));

        Products product = createProduct(
                List.of(new ProductImages("http://test.jpg", 0)),
                List.of(productOptionTypes),
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
        OptionTypes optionTypes = new OptionTypes("optionType");
        OptionValues optionValue1 = createOptionValue(1L, "optionValue1", optionTypes);
        OptionValues optionValue2 = createOptionValue(2L, "optionValue2", optionTypes);

        ProductOptionTypes productOptionTypes = createProductOptionTypes(optionTypes);
        ProductVariantOptions productVariantOption1 = new ProductVariantOptions(optionValue1);
        ProductVariantOptions productVariantOption2 = new ProductVariantOptions(optionValue2);
        ProductVariants productVariant1 = createProductVariant(1L, "sku1", List.of(productVariantOption1));
        ProductVariants productVariant2 = createProductVariant(2L, "sku2", List.of(productVariantOption2));

        Products product = createProduct(
                List.of(new ProductImages("http://test.jpg", 0)),
                List.of(productOptionTypes),
                List.of(productVariant1, productVariant2));
        when(productVariantsRepository.findWithProductById(1L))
                .thenReturn(Optional.of(productVariant1));

        productVariantService.deleteVariantById(1L);

        assertThat(product.getProductVariants().size()).isEqualTo(1);
        assertThat(product.getProductVariants())
                .extracting(ProductVariants::getId, ProductVariants::getSku, ProductVariants::getPrice,
                        ProductVariants::getStockQuantity, ProductVariants::getDiscountValue)
                .containsExactlyInAnyOrder(
                        tuple(2L, "sku2", 10000, 100, 10)
                );

        assertThat(product.getProductVariants())
                .flatExtracting(ProductVariants::getProductVariantOptions)
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
        OptionTypes optionTypes = new OptionTypes("optionType");
        OptionValues optionValues = new OptionValues("optionValue");
        optionTypes.addOptionValue(optionValues);

        ProductOptionTypes productOptionTypes = createProductOptionTypes(optionTypes);
        ProductVariantOptions productVariantOption = new ProductVariantOptions(optionValues);
        ProductVariants productVariant = createProductVariant(1L,"sku", List.of(productVariantOption));

        Products product = createProduct(
                List.of(new ProductImages("http://test.jpg", 0)),
                List.of(productOptionTypes),
                List.of(productVariant));
        when(productVariantsRepository.findWithProductById(1L))
                .thenReturn(Optional.of(productVariant));

        assertThatThrownBy(() -> productVariantService.deleteVariantById(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("must be at least one product variant per product");
    }

    private Products createProduct(List<ProductImages> productImages, List<ProductOptionTypes> productOptionTypes,
                                   List<ProductVariants> productVariants){
        Products product = new Products("productName", "product description",
                new Categories("category", "http://test.jpg"));

        product.addImages(productImages);
        product.addOptionTypes(productOptionTypes);
        product.addVariants(productVariants);
        return product;
    }

    private ProductOptionTypes createProductOptionTypes(OptionTypes optionTypes){
        return new ProductOptionTypes(optionTypes, 0, true);
    }

    private ProductVariants createProductVariant(Long variantId, String sku, List<ProductVariantOptions> productVariantOptions){
        ProductVariants productVariant = new ProductVariants(sku, 10000, 100, 10);
        productVariant.addProductVariantOptions(productVariantOptions);
        ReflectionTestUtils.setField(productVariant, "id", variantId);
        return productVariant;
    }

    private OptionValues createOptionValue(Long id, String name, OptionTypes optionTypes){
        OptionValues optionValue = new OptionValues(name);
        ReflectionTestUtils.setField(optionValue, "id", id);
        optionTypes.addOptionValue(optionValue);
        return optionValue;
    }
}
