package com.example.product_service.service.unit;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.request.image.AddImageRequest;
import com.example.product_service.dto.request.image.ImageRequest;
import com.example.product_service.dto.request.options.ProductOptionTypeRequest;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.product.UpdateProductBasicRequest;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
import com.example.product_service.dto.request.variant.VariantOptionValueRequest;
import com.example.product_service.dto.response.image.ImageResponse;
import com.example.product_service.dto.response.product.ProductResponse;
import com.example.product_service.dto.response.product.ProductUpdateResponse;
import com.example.product_service.dto.response.variant.ProductVariantResponse;
import com.example.product_service.entity.*;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.exception.DuplicateResourceException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.*;
import com.example.product_service.service.ProductSaver;
import com.example.product_service.service.dto.ProductCreationData;
import com.example.product_service.service.dto.ProductVariantCreationData;
import com.example.product_service.service.util.factory.ProductFactory;
import com.example.product_service.service.util.validator.ProductReferentialService;
import com.example.product_service.service.util.validator.RequestValidator;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.example.product_service.common.MessagePath.*;
import static com.example.product_service.util.TestMessageUtil.getMessage;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductSaverUnitTest {
    @Mock
    ProductsRepository productsRepository;
    @Mock
    RequestValidator structureValidator;
    @Mock
    ProductReferentialService referentialValidator;
    @Mock
    ProductFactory factory;
    @Mock
    CategoryRepository categoryRepository;
    @Mock
    MessageSourceUtil ms;


    @Captor
    private ArgumentCaptor<Products> productArgumentCaptor;
    @InjectMocks
    ProductSaver productSaver;

    @Test
    @DisplayName("상품 저장 테스트-성공")
    void saveProductTest_unit_success(){
        ProductRequest request = createProductRequest();

        doNothing().when(structureValidator).validateProductRequest(request);
        ProductCreationData creationData = createProductCreationData();
        when(referentialValidator.validAndFetch(request)).thenReturn(creationData);
        Products built = createProducts();
        when(factory.createProducts(request, creationData)).thenReturn(built);
        when(productsRepository.save(any(Products.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = productSaver.saveProduct(request);

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

        assertThatThrownBy(() -> productSaver.saveProduct(request))
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

        assertThatThrownBy(() -> productSaver.saveProduct(request))
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

        assertThatThrownBy(() -> productSaver.saveProduct(request))
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
        assertThatThrownBy(() -> productSaver.saveProduct(request))
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
        assertThatThrownBy(() -> productSaver.saveProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_VARIANT_OPTION_VALUE_CONFLICT));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(동일한 SKU가 DB에 존재하는 경우)")
    void saveProductTest_unit_conflict_sku(){
        ProductRequest request = createProductRequest();
        mockStructureValidator(request, null);
        mockReferentialValidator(request, null, PRODUCT_VARIANT_SKU_CONFLICT);
        assertThatThrownBy(() -> productSaver.saveProduct(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage(getMessage(PRODUCT_VARIANT_SKU_CONFLICT));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(카테고리 없음)")
    void saveProductTest_unit_category_notFound(){
        ProductRequest request = createProductRequest();
        mockStructureValidator(request, null);
        mockReferentialValidator(request, null, CATEGORY_NOT_FOUND);
        assertThatThrownBy(() -> productSaver.saveProduct(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(CATEGORY_NOT_FOUND));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(최하위 카테고리가 이님)")
    void saveProductTest_unit_category_notLeaf(){
        ProductRequest request = createProductRequest();
        mockStructureValidator(request, null);
        mockReferentialValidator(request, null, PRODUCT_CATEGORY_BAD_REQUEST);

        assertThatThrownBy(() -> productSaver.saveProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_CATEGORY_BAD_REQUEST));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(옵션 타입 없음)")
    void saveProductTest_unit_optionType_notFound(){
        ProductRequest request = createProductRequest();
        mockStructureValidator(request, null);
        mockReferentialValidator(request, null, OPTION_TYPE_NOT_FOUND);
        assertThatThrownBy(() -> productSaver.saveProduct(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(OPTION_TYPE_NOT_FOUND));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(상품 변형 옵션 값이 옵션 타입의 연관 객체가 아닌경우)")
    void saveProductTest_unit_optionValue_notMatch_type(){
        ProductRequest request = createProductRequest();
        mockStructureValidator(request, null);
        mockReferentialValidator(request, null, PRODUCT_OPTION_VALUE_NOT_MATCH_TYPE);
        assertThatThrownBy(() -> productSaver.saveProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_OPTION_VALUE_NOT_MATCH_TYPE));
    }

    @Test
    @DisplayName("상품 기본 정보 수정 테스트-성공")
    void updateBasicInfoByIdTest_unit_success(){
        UpdateProductBasicRequest request = new UpdateProductBasicRequest("updateName", null, null);
        Products products = createProducts();
        when(productsRepository.findById(1L)).thenReturn(Optional.of(products));
        ProductUpdateResponse response = productSaver.updateBasicInfoById(1L, request);

        assertThat(response.getName()).isEqualTo("updateName");
        assertThat(response.getCategoryId()).isEqualTo(1L);
        assertThat(response.getDescription()).isEqualTo("description");

    }

    @Test
    @DisplayName("상품 기본 정보 수정 테스트-실패(상품을 찾을 수 없음)")
    void updateBasicInfoByIdTest_unit_notFound_product(){
        UpdateProductBasicRequest request = new UpdateProductBasicRequest("updateName", null, null);
        when(productsRepository.findById(1L)).thenReturn(Optional.empty());
        when(ms.getMessage(PRODUCT_NOT_FOUND)).thenReturn("Product not found");
        assertThatThrownBy(() -> productSaver.updateBasicInfoById(1L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(PRODUCT_NOT_FOUND));
    }

    @Test
    @DisplayName("상품 기본 정보 수정 테스트-실패(카테고리를 찾을 수 없음)")
    void updateBasicInfoByIdTest_unit_notFound_category(){
        UpdateProductBasicRequest request = new UpdateProductBasicRequest("updateName", null, 2L);
        Products products = createProducts();
        when(productsRepository.findById(1L)).thenReturn(Optional.of(products));
        when(categoryRepository.findById(2L)).thenReturn(Optional.empty());
        when(ms.getMessage(CATEGORY_NOT_FOUND)).thenReturn("Category not found");
        assertThatThrownBy(() -> productSaver.updateBasicInfoById(1L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(CATEGORY_NOT_FOUND));
    }

    @Test
    @DisplayName("상품 삭제 테스트-성공")
    void deleteProductByIdTest_unit_success(){
        Products products = spy(createProducts());
        when(productsRepository.findById(1L)).thenReturn(Optional.of(products));

        productSaver.deleteProductById(1L);

        verify(productsRepository).delete(productArgumentCaptor.capture());
        Products value = productArgumentCaptor.getValue();
        assertThat(value.getName()).isEqualTo("name");

    }

    @Test
    @DisplayName("상품 삭제 테스트-실패(상품을 찾을 수 없음)")
    void deleteProductByIdTest_unit_notFoundProduct(){
        when(productsRepository.findById(1L)).thenReturn(Optional.empty());
        when(ms.getMessage(PRODUCT_NOT_FOUND)).thenReturn("Product not found");

        assertThatThrownBy(() -> productSaver.deleteProductById(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(PRODUCT_NOT_FOUND));
    }

    @Test
    @DisplayName("상품 이미지 추가 테스트-성공")
    void addImagesTest_unit_success(){
        AddImageRequest request = new AddImageRequest(List.of("http://test2.jpg", "http://test3.jpg"));
        Products products = createProducts();
        when(productsRepository.findById(1L)).thenReturn(Optional.of(products));

        List<ImageResponse> response = productSaver.addImages(1L, request);

        assertThat(response.size()).isEqualTo(3);
        assertThat(response).extracting("url", "sortOrder")
                .containsExactlyInAnyOrder(
                        tuple("http://test.jpg", 0),
                        tuple("http://test2.jpg", 1),
                        tuple("http://test3.jpg", 2)
                );
    }

    @Test
    @DisplayName("상품 이미지 추가 테스트-실패(상품을 찾을 수 없음)")
    void addImageTest_unit_notFoundProduct(){
        AddImageRequest request = new AddImageRequest(List.of("http://test2.jpg", "http://test3.jpg"));
        when(productsRepository.findById(1L)).thenReturn(Optional.empty());
        when(ms.getMessage(PRODUCT_NOT_FOUND)).thenReturn("Product not found");
        assertThatThrownBy(() -> productSaver.addImages(1L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(PRODUCT_NOT_FOUND));
    }

    @Test
    @DisplayName("상품 변형 추가 테스트-성공")
    void addVariantTest_unit_success(){
        ProductVariantRequest request = new ProductVariantRequest("sku2", 3000, 100, 10, List.of(
                new VariantOptionValueRequest(1L, 7L)
        ));
        Products products = createProducts();
        when(productsRepository.findById(1L)).thenReturn(Optional.of(products));
        doNothing().when(structureValidator).validateVariantRequest(request);
        ProductVariantCreationData creationData = new ProductVariantCreationData(createOptionValueById(7L));
        when(referentialValidator.validateProductVariant(request))
                .thenReturn(creationData);
        OptionTypes optionType = createOptionTypesWithSetId(1L, "optionType");
        OptionValues optionValue = createOptionValuesWithSetId(7L, "optionValue7");
        optionType.addOptionValue(optionValue);
        ProductVariantOptions productVariantOptions = new ProductVariantOptions(optionValue);
        ProductVariants productVariants = new ProductVariants("sku2", 3000, 100, 10);
        productVariants.addProductVariantOption(productVariantOptions);
        when(factory.createProductVariant(request, creationData))
                .thenReturn(productVariants);

        ProductVariantResponse response = productSaver.addVariant(1L, request);

        assertThat(response.getSku()).isEqualTo("sku2");
        assertThat(response.getPrice()).isEqualTo(3000);
        assertThat(response.getStockQuantity()).isEqualTo(100);
        assertThat(response.getDiscountRate()).isEqualTo(10);
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
        if(exceptionCode.equals(CATEGORY_NOT_FOUND)){
            when.thenThrow(new NotFoundException(getMessage(CATEGORY_NOT_FOUND)));
        } else if (exceptionCode.equals(OPTION_TYPE_NOT_FOUND)){
            when.thenThrow(new NotFoundException(getMessage(OPTION_TYPE_NOT_FOUND)));
        } else if (exceptionCode.equals(PRODUCT_VARIANT_SKU_CONFLICT)){
            when.thenThrow(new DuplicateResourceException(getMessage(PRODUCT_VARIANT_SKU_CONFLICT)));
        } else if (exceptionCode.equals(PRODUCT_OPTION_VALUE_NOT_MATCH_TYPE)){
            when.thenThrow(new BadRequestException(getMessage(PRODUCT_OPTION_VALUE_NOT_MATCH_TYPE)));
        } else if (exceptionCode.equals(PRODUCT_CATEGORY_BAD_REQUEST)){
            when.thenThrow(new BadRequestException(getMessage(PRODUCT_CATEGORY_BAD_REQUEST)));
        }
    }

    private ProductRequest createProductRequest(){
        return new ProductRequest(
                "name",
                "description",
                1L,
                List.of(new ImageRequest("http://test.jpg")),
                List.of(new ProductOptionTypeRequest(1L, 1)),
                List.of(new ProductVariantRequest("sku", 3000, 100, 10, List.of(
                        new VariantOptionValueRequest(1L, 5L)
                )))
        );
    }
    private Products createProducts(){
        Products product = new Products("name", "description", createCategory(1L));
        product.addImages(List.of(new ProductImages("http://test.jpg", 0)));
        OptionTypes optionType = createOptionTypesWithSetId(1L, "optionType");
        ProductOptionTypes productOptionType = new ProductOptionTypes(optionType, 1, true);
        product.addOptionTypes(List.of(productOptionType));

        OptionValues optionValue = createOptionValuesWithSetId(5L, "optionValue");

        optionType.addOptionValue(optionValue);
        ProductVariants productVariant = new ProductVariants("sku", 3000, 100, 10);
        ProductVariantOptions productVariantOption = new ProductVariantOptions(optionValue);
        productVariant.addProductVariantOptions(List.of(productVariantOption));

        product.addVariants(List.of(productVariant));
        return product;
    }
    private OptionTypes createOptionTypesWithSetId(Long id, String name){
        OptionTypes optionTypes = new OptionTypes(name);
        ReflectionTestUtils.setField(optionTypes, "id", id);
        return optionTypes;
    }

    private OptionValues createOptionValuesWithSetId(Long id, String name){
        OptionValues optionValues = new OptionValues(name);
        ReflectionTestUtils.setField(optionValues, "id", id);
        return optionValues;
    }

    private ProductCreationData createProductCreationData(){
        return new ProductCreationData(
                createCategory(1L),
                createOptionTypeById(1L),
                createOptionValueById(5L)
        );
    }

    private Categories createCategory(Long id){
        Categories category = new Categories("category", "http://test.jpg");
        ReflectionTestUtils.setField(category, "id", id);
        return category;
    }

    private Map<Long, OptionTypes> createOptionTypeById(Long... ids){
        Map<Long, OptionTypes> map = new HashMap<>();
        for (Long id : ids) {
            OptionTypes optionType = new OptionTypes("optionType" + id);
            ReflectionTestUtils.setField(optionType, "id", id);
            map.put(id, optionType);
        }
        return map;
    }

    private Map<Long, OptionValues> createOptionValueById(Long... ids){
        Map<Long, OptionValues> map = new HashMap<>();
        for (Long id : ids) {
            OptionValues optionValue = new OptionValues("optionValue" + id);
            ReflectionTestUtils.setField(optionValue, "id", id);
            map.put(id, optionValue);
        }
        return map;
    }
}
