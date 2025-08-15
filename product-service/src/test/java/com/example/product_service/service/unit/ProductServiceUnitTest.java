package com.example.product_service.service.unit;

import com.example.product_service.dto.request.image.ImageRequest;
import com.example.product_service.dto.request.options.ProductOptionTypeRequest;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
import com.example.product_service.dto.request.variant.VariantOptionValueRequest;
import com.example.product_service.dto.response.product.ProductResponse;
import com.example.product_service.entity.Products;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.exception.DuplicateResourceException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.*;
import com.example.product_service.service.ProductService;
import com.example.product_service.service.dto.ProductCreationData;
import com.example.product_service.service.util.ProductFactory;
import com.example.product_service.service.util.ProductReferentialValidator;
import com.example.product_service.service.util.ProductRequestStructureValidator;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;

import java.util.List;

import static com.example.product_service.common.MessagePath.*;
import static com.example.product_service.util.TestMessageUtil.getMessage;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceUnitTest {
    @Mock
    ProductsRepository productsRepository;
    @Mock
    ProductRequestStructureValidator structureValidator;
    @Mock
    ProductReferentialValidator referentialValidator;
    @Mock
    ProductFactory factory;

    @InjectMocks
    ProductService productService;

    @Test
    @DisplayName("상품 저장 테스트-성공")
    void saveProductTest_unit_success(){
        ProductRequest request = createProductRequest();
        when(productsRepository.save(any(Products.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        ProductResponse response = productService.saveProduct(request);

        assertThat(response.getName()).isEqualTo("name");
        assertThat(response.getDescription()).isEqualTo("description");
        assertThat(response.getCategoryId()).isEqualTo(1L);
        assertThat(response.getImages())
                .extracting("url", "sortOrder")
                .containsExactlyInAnyOrder(
                        Tuple.tuple("http://test.jpg", 0)
                );
        assertThat(response.getProductOptionTypes())
                .extracting("id", "name")
                .containsExactlyInAnyOrder(
                        tuple(1L, "optionType")
                );

        assertThat(response.getProductVariants())
                .extracting( "sku", "price", "stockQuantity", "discountRate")
                .containsExactlyInAnyOrder(
                        tuple("sku", 3000, 100, 10)
                );

        assertThat(response.getProductVariants())
                .flatExtracting("optionValues")
                .extracting("valueId", "typeId", "valueName")
                .containsExactlyInAnyOrder(
                        tuple (5L, 1L, "optionValue")
                );
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(요청 Body에 중복된 productOptionTypeId)")
    void saveProductTest_unit_product_option_type_typeId_badRequest(){
        ProductRequest request = createProductRequest();
        request.setProductOptionTypes(
                List.of(new ProductOptionTypeRequest(1L, 0),
                        new ProductOptionTypeRequest(1L,1))
        );
        mockStructureValidator(request, PRODUCT_OPTION_TYPE_TYPE_BAD_REQUEST);

        assertThatThrownBy(() -> productService.saveProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_OPTION_TYPE_TYPE_BAD_REQUEST));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(요청 Body에 중복된 priority)")
    void saveProductTest_unit_product_option_type_priority_badRequest(){
        ProductRequest request = createProductRequest();
        request.setProductOptionTypes(
                List.of(new ProductOptionTypeRequest(1L, 0),
                        new ProductOptionTypeRequest(2L, 0))
        );
        mockStructureValidator(request, PRODUCT_OPTION_TYPE_PRIORITY_BAD_REQUEST);

        assertThatThrownBy(() -> productService.saveProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_OPTION_TYPE_PRIORITY_BAD_REQUEST));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(요청 Body에 중복된 SKU)")
    void saveProductTest_unit_product_variant_sku_badRequest(){
        ProductRequest request = createProductRequest();
        request.setProductVariants(
                List.of(new ProductVariantRequest("sku", 3000, 100, 10, List.of(
                                new VariantOptionValueRequest(1L, 5L))),
                        new ProductVariantRequest("sku", 3000, 100, 10, List.of(
                                new VariantOptionValueRequest(1L, 7L)))
                )
        );
        mockStructureValidator(request, PRODUCT_VARIANT_SKU_CONFLICT);

        assertThatThrownBy(() -> productService.saveProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_VARIANT_SKU_CONFLICT));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(상품 옵션 타입과 일치하지 않은 상품 변형 옵션이 들어올 경우)")
    void saveProductTest_unit_optionType_cardinality_violation(){
        ProductRequest request = createProductRequest();
        request.setProductVariants(List.of(
                new ProductVariantRequest("sku",
                                100,
                                100,
                                10, List.of(new VariantOptionValueRequest(5L, 1L))))
        );

        mockStructureValidator(request, PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION);
        assertThatThrownBy(() -> productService.saveProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(상품 변형의 옵션 값이 중복될 경우)")
    void saveProductTest_unit_optionValue_duplicate(){
        ProductRequest request = createProductRequest();
        request.setProductOptionTypes(
                List.of(new ProductOptionTypeRequest(1L, 1),
                        new ProductOptionTypeRequest(2L, 2))
        );
        request.setProductVariants(
                List.of(new ProductVariantRequest("sku1",
                                100,
                                100,
                                10,
                                List.of(new VariantOptionValueRequest(1L, 1L),
                                        new VariantOptionValueRequest(2L, 1L))),
                        new ProductVariantRequest("sku2",
                                100,
                                100,
                                10,
                                List.of(new VariantOptionValueRequest(1L,1L),
                                        new VariantOptionValueRequest(2L,1L)))
                )
        );
        mockStructureValidator(request, PRODUCT_VARIANT_OPTION_VALUE_CONFLICT);
        assertThatThrownBy(() -> productService.saveProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_VARIANT_OPTION_VALUE_CONFLICT));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(카테고리 없음)")
    void saveProductTest_unit_category_notFound(){
        ProductRequest request = createProductRequest();
        mockStructureValidator(request, null);
        mockReferentialValidator(request, null, CATEGORY_NOT_FOUND);
        assertThatThrownBy(() -> productService.saveProduct(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(CATEGORY_NOT_FOUND));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(옵션 타입 없음)")
    void saveProductTest_unit_optionType_notFound(){
        ProductRequest request = createProductRequest();
        mockStructureValidator(request, null);
        mockReferentialValidator(request, null, OPTION_TYPE_NOT_FOUND);
        assertThatThrownBy(() -> productService.saveProduct(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(OPTION_TYPE_NOT_FOUND));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(ProductRequest 의 Variant option 중 optionTypeId 가 ProductOptionType optionTypeId와 다를때)")
    void saveProductTest_unit_variantOptionValue_cardinality_violation1(){
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(상품 옵션보다 상품 변형 옵션이 더 많이 추가되었을때)")
    void saveProductTest_unit_variantOptionValue_cardinality_violation2(){
        ProductRequest request = new ProductRequest(
                "name",
                "description",
                1L,
                List.of(new ImageRequest("http://test.jpg", 0)),
                List.of(new ProductOptionTypeRequest(1L, 1)),
                List.of(new ProductVariantRequest("sku", 3000, 100, 10,
                        List.of(new VariantOptionValueRequest(1L, 5L),
                                new VariantOptionValueRequest(2L, 7L)
                        )))
        );
        assertThatThrownBy(() -> productService.saveProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(상품 옵션보다 상품 변형 옵션이 더 적게 추가되었을때)")
    void saveProductTest_unit_variantOptionValue_cardinality_violation3(){
        ProductRequest request = new ProductRequest(
                "name",
                "description",
                1L,
                List.of(new ImageRequest("http://test.jpg", 0)),
                List.of(new ProductOptionTypeRequest(1L, 1), new ProductOptionTypeRequest(2L, 2)),
                List.of(new ProductVariantRequest("sku", 3000, 100, 10,
                        List.of(new VariantOptionValueRequest(1L, 5L)
                        )))
        );
        assertThatThrownBy(() -> productService.saveProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(상품 변형 옵션중 동일한 옵션 타입을 가지는 값이 여러개일때)")
    void saveProductTest_unit_variantOptionValue_cardinality_violation4(){
        ProductRequest request = new ProductRequest(
                "name",
                "description",
                1L,
                List.of(new ImageRequest("http://test.jpg", 0)),
                List.of(new ProductOptionTypeRequest(1L, 1), new ProductOptionTypeRequest(2L, 2)),
                List.of(new ProductVariantRequest("sku", 3000, 100, 10,
                        List.of(new VariantOptionValueRequest(1L, 5L),
                                new VariantOptionValueRequest(1L, 8L)
                        )))
        );
        assertThatThrownBy(() -> productService.saveProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(상품 변형의 옵션 값이 상품 옵션 타입의 옵션값이 아닌경우)")
    void saveProductTest_unit_variantOptionValue_optionValue_notMatchedType(){
        ProductRequest request = new ProductRequest(
                "name",
                "description",
                1L,
                List.of(new ImageRequest("http://test.jpg", 0)),
                List.of(new ProductOptionTypeRequest(1L, 1)),
                List.of(new ProductVariantRequest("sku", 3000, 100, 10,
                        List.of(new VariantOptionValueRequest(1L, 5L)
                        )))
        );
        assertThatThrownBy(() -> productService.saveProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_OPTION_VALUE_NOT_MATCH_TYPE));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(SKU가 중복될 경우)")
    void saveProductTest_unit_variantSKU_conflict(){
        ProductRequest request = new ProductRequest(
                "name",
                "description",
                1L,
                List.of(new ImageRequest("http://test.jpg", 0)),
                List.of(new ProductOptionTypeRequest(1L, 1)),
                List.of(new ProductVariantRequest("sku", 3000, 100, 10,
                        List.of(new VariantOptionValueRequest(1L, 5L))))
        );
        assertThatThrownBy(() -> productService.saveProduct(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage(getMessage(PRODUCT_VARIANT_SKU_CONFLICT));
    }

    private ProductRequest createProductRequest(){
        return new ProductRequest(
                "name",
                "description",
                1L,
                List.of(new ImageRequest("http://test.jpg", 0)),
                List.of(new ProductOptionTypeRequest(1L, 1)),
                List.of(new ProductVariantRequest("sku", 3000, 100, 10, List.of(
                        new VariantOptionValueRequest(1L, 5L)
                )))
        );
    }

    private void mockStructureValidator(ProductRequest request, String exceptionCode){
        if(exceptionCode == null){
            doNothing().when(structureValidator).validateProductRequest(request);
        } else if (exceptionCode.equals(PRODUCT_OPTION_TYPE_TYPE_BAD_REQUEST)){
            doThrow(new BadRequestException(getMessage(PRODUCT_OPTION_TYPE_TYPE_BAD_REQUEST)))
                    .when(structureValidator).validateProductRequest(request);
        } else if (exceptionCode.equals(PRODUCT_OPTION_TYPE_PRIORITY_BAD_REQUEST)){
            doThrow(new BadRequestException(getMessage(PRODUCT_OPTION_TYPE_PRIORITY_BAD_REQUEST)))
                    .when(structureValidator).validateProductRequest(request);
        } else if (exceptionCode.equals(PRODUCT_VARIANT_SKU_CONFLICT)){
            doThrow(new BadRequestException(getMessage(PRODUCT_VARIANT_SKU_CONFLICT)))
                    .when(structureValidator).validateProductRequest(request);
        } else if (exceptionCode.equals(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION)){
            doThrow(new BadRequestException(getMessage(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION)))
                    .when(structureValidator).validateProductRequest(request);
        } else if (exceptionCode.equals(PRODUCT_VARIANT_OPTION_VALUE_CONFLICT)){
            doThrow(new BadRequestException(getMessage(PRODUCT_VARIANT_OPTION_VALUE_CONFLICT)))
                    .when(structureValidator).validateProductRequest(request);
        }
    }

    private void mockReferentialValidator(ProductRequest request, ProductCreationData returnData, String exceptionCode){
        OngoingStubbing<ProductCreationData> when = when(referentialValidator.validAndFetch(request));
        if(exceptionCode == CATEGORY_NOT_FOUND){
            when.thenThrow(new NotFoundException(getMessage(CATEGORY_NOT_FOUND)));
        } else if (exceptionCode == OPTION_TYPE_NOT_FOUND){
            when.thenThrow(new NotFoundException(getMessage(OPTION_TYPE_NOT_FOUND)));
        }
    }

}
