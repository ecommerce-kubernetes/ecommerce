package com.example.product_service.service.unit;

import com.example.product_service.common.MessagePath;
import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.request.image.ImageRequest;
import com.example.product_service.dto.request.options.ProductOptionTypeRequest;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
import com.example.product_service.dto.request.variant.VariantOptionValueRequest;
import com.example.product_service.dto.response.product.ProductResponse;
import com.example.product_service.entity.*;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.exception.DuplicateResourceException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.CategoryRepository;
import com.example.product_service.repository.ProductsRepository;
import com.example.product_service.service.ProductApplicationService;
import com.example.product_service.service.dto.ProductCreationData;
import com.example.product_service.service.util.factory.ProductFactory;
import com.example.product_service.service.util.validator.ProductReferentialService;
import com.example.product_service.service.util.validator.RequestValidator;
import com.example.product_service.util.TestMessageUtil;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.product_service.common.MessagePath.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductApplicationServiceTest {
    @Mock
    ProductsRepository productsRepository;
    @Mock
    RequestValidator requestValidator;
    @Mock
    ProductReferentialService productReferentialService;
    @Mock
    ProductFactory factory;
    @Mock
    CategoryRepository categoryRepository;
    @Mock
    MessageSourceUtil ms;

    @Captor
    private ArgumentCaptor<Products> productArgumentCaptor;
    @InjectMocks
    ProductApplicationService productApplicationService;

    @Test
    @DisplayName("상품 저장 테스트-성공")
    void saveProductTest_unit_success(){
        Categories category = createCategory(1L, "핸드폰", "http://phone.jpg");
        OptionTypes storage = createOptionType(1L, "용량");
        OptionTypes color = createOptionType(2L, "색상");
        OptionValues gb_128 = createOptionValue(1L, "128GB", storage);
        OptionValues red = createOptionValue(2L, "빨강", color);

        Map<Long, OptionTypes> optionTypeById = createOptionTypeMap(storage, color);
        Map<Long, OptionValues> optionValueById = createOptionValueMap(gb_128, red);

        ProductCreationData creationData = new ProductCreationData(category, optionTypeById, optionValueById);
        ProductRequest request = createProductRequest();
        Products product = createProduct(request, category, creationData);

        doNothing().when(requestValidator).validateProductRequest(request);
        when(productReferentialService.validAndFetch(request))
                .thenReturn(creationData);
        when(factory.createProducts(request, creationData))
                .thenReturn(product);
        when(productsRepository.save(any(Products.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = productApplicationService.saveProduct(request);


        assertThat(response.getName()).isEqualTo("아이폰 16 Pro");
        assertThat(response.getDescription()).isEqualTo("애플 IPhone 16 (Model-Pro)");
        assertThat(response.getCategoryId()).isEqualTo(category.getId());
        assertThat(response.getImages())
                .extracting("url", "sortOrder")
                .containsExactlyInAnyOrder(
                        tuple("http://iphone16-1.jpg", 0),
                        tuple("http://iphone16-2.jpg", 1)
                );

        assertThat(response.getProductOptionTypes())
                .extracting("id", "name")
                .containsExactlyInAnyOrder(
                        tuple(1L, "용량"),
                        tuple(2L, "색상")
                );

        assertThat(response.getProductVariants())
                .extracting( "sku", "price", "stockQuantity", "discountRate")
                .containsExactlyInAnyOrder(
                        tuple("IPHONE16Pro-128GB-RED", 1200000, 100, 5)
                );


        assertThat(response.getProductVariants())
                .flatExtracting("optionValues")
                .extracting("valueId", "typeId", "valueName")
                .containsExactlyInAnyOrder(
                        tuple (1L, 1L, "128GB"),
                        Tuple.tuple(2L, 2L, "빨강")
                );
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(요청 바디의 productOptionType에 optionTypeId가 중복)")
    void saveProductTest_unit_request_product_option_type_typeId_duplicate(){
        ProductRequest request = createProductRequest();
        //요청 Body의 ProductOptionTypeRequest 리스트에 동일한 OptionTypeId가 존재
        request.setProductOptionTypes(
                List.of(new ProductOptionTypeRequest(1L, 0), new ProductOptionTypeRequest(1L, 1))
        );

        //RequestValidator 가 예외를 던짐
        doThrow(new BadRequestException(TestMessageUtil.getMessage(PRODUCT_OPTION_TYPE_TYPE_BAD_REQUEST)))
                .when(requestValidator).validateProductRequest(request);

        assertThatThrownBy(() -> productApplicationService.saveProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(TestMessageUtil.getMessage(PRODUCT_OPTION_TYPE_TYPE_BAD_REQUEST));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(요청 바디의 productOptionType에 priority가 중복)")
    void saveProductTest_unit_request_product_option_type_priority_duplicate(){
        ProductRequest request = createProductRequest();
        //요청 Body의 ProductOptionTypeRequest 리스트에 동일한 priority가 존재
        request.setProductOptionTypes(
                List.of(new ProductOptionTypeRequest(1L, 0), new ProductOptionTypeRequest(2L, 0))
        );

        //RequestValidator 가 예외를 던짐
        doThrow(new BadRequestException(TestMessageUtil.getMessage(PRODUCT_OPTION_TYPE_PRIORITY_BAD_REQUEST)))
                .when(requestValidator).validateProductRequest(request);

        assertThatThrownBy(() -> productApplicationService.saveProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(TestMessageUtil.getMessage(PRODUCT_OPTION_TYPE_PRIORITY_BAD_REQUEST));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(요청 바디의 ProductVariant 리스트에 SKU 가 중복)")
    void saveProductTest_unit_request_product_variant_sku_duplicate(){
        ProductRequest request = createProductRequest();
        //요청 Body의 ProductVariant 리스트에 동일한 SKU 가 존재
        request.setProductVariants(
                List.of(
                        new ProductVariantRequest("아이폰 16", 1200000, 100, 5,
                                List.of(new VariantOptionValueRequest(1L, 1L),
                                        new VariantOptionValueRequest(2L, 2L))),
                        new ProductVariantRequest("아이폰 16", 1200000, 100, 5,
                                List.of(new VariantOptionValueRequest(1L, 4L),
                                        new VariantOptionValueRequest(2L, 2L)))
                )
        );

        //RequestValidator 가 예외를 던짐
        doThrow(new BadRequestException(TestMessageUtil.getMessage(PRODUCT_VARIANT_SKU_CONFLICT)))
                .when(requestValidator).validateProductRequest(request);

        assertThatThrownBy(() -> productApplicationService.saveProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(TestMessageUtil.getMessage(PRODUCT_VARIANT_SKU_CONFLICT));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(요청 바디의 productOptionType과 VariantOption이 일치하지 않는 경우" +
            ".1 VariantOption size > productOptionType size)")
    void saveProductTest_unit_request_product_variant_option_badRequest1(){
        ProductRequest request = createProductRequest();

        //Variant Option이 ProductOptionType 보다 많음
        request.setProductVariants(
                List.of(
                        new ProductVariantRequest("아이폰 16", 1200000, 100, 5,
                                List.of(new VariantOptionValueRequest(1L, 1L),
                                        new VariantOptionValueRequest(2L, 2L),
                                        new VariantOptionValueRequest(3L, 3L)))
                )
        );

        //RequestValidator 가 예외를 던짐
        doThrow(new BadRequestException(TestMessageUtil.getMessage(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION)))
                .when(requestValidator).validateProductRequest(request);


        assertThatThrownBy(() -> productApplicationService.saveProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(TestMessageUtil.getMessage(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(요청 바디의 productOptionType과 VariantOption이 일치하지 않는 경우" +
            ".2 VariantOption size < productOptionType size)")
    void saveProductTest_unit_request_product_variant_option_badRequest2(){
        ProductRequest request = createProductRequest();

        //Variant Option이 ProductOptionType 보다 적음
        request.setProductVariants(
                List.of(
                        new ProductVariantRequest("아이폰 16", 1200000, 100, 5,
                                List.of(new VariantOptionValueRequest(1L, 1L)))
                )
        );

        //RequestValidator 가 예외를 던짐
        doThrow(new BadRequestException(TestMessageUtil.getMessage(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION)))
                .when(requestValidator).validateProductRequest(request);

        assertThatThrownBy(() -> productApplicationService.saveProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(TestMessageUtil.getMessage(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(요청 바디의 productOptionType과 VariantOption이 일치하지 않는 경우" +
            ".3 VariantOption != productOptionType)")
    void saveProductTest_unit_request_product_variant_option_badRequest3(){
        ProductRequest request = createProductRequest();

        //Variant Option와 ProductOptionType이 일치하지 않음
        request.setProductVariants(
                List.of(
                        new ProductVariantRequest("아이폰 16", 1200000, 100, 5,
                                List.of(new VariantOptionValueRequest(1L, 1L),
                                        new VariantOptionValueRequest(3L, 3L)))
                )
        );

        //RequestValidator 가 예외를 던짐
        doThrow(new BadRequestException(TestMessageUtil.getMessage(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION)))
                .when(requestValidator).validateProductRequest(request);


        assertThatThrownBy(() -> productApplicationService.saveProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(TestMessageUtil.getMessage(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(요청 바디의 ProductVariant Option 조합이 중복될 경우)")
    void saveProductTest_unit_request_product_variant_option_combination_duplicate(){
        ProductRequest request = createProductRequest();

        //Variant Option와 ProductOptionType이 일치하지 않음
        request.setProductVariants(
                List.of(
                        new ProductVariantRequest("아이폰 16-1", 1200000, 100, 5,
                                List.of(new VariantOptionValueRequest(1L, 1L),
                                        new VariantOptionValueRequest(2L, 2L))),
                        new ProductVariantRequest("아이폰 16-2", 1200000, 100, 5,
                                List.of(new VariantOptionValueRequest(1L, 1L),
                                        new VariantOptionValueRequest(2L, 2L)))
                )
        );

        //RequestValidator 가 예외를 던짐
        doThrow(new BadRequestException(TestMessageUtil.getMessage(PRODUCT_VARIANT_OPTION_VALUE_CONFLICT)))
                .when(requestValidator).validateProductRequest(request);

        assertThatThrownBy(() -> productApplicationService.saveProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(TestMessageUtil.getMessage(PRODUCT_VARIANT_OPTION_VALUE_CONFLICT));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(동일한 SKU 가 DB 에 존재하는 경우)")
    void saveProductTest_unit_conflict_sku(){
        ProductRequest request = createProductRequest();

        //요청 Body 검증 통과
        doNothing().when(requestValidator).validateProductRequest(request);

        //DB 중복 예외를 던짐
        doThrow(new DuplicateResourceException(TestMessageUtil.getMessage(PRODUCT_VARIANT_SKU_CONFLICT)))
                .when(productReferentialService).validAndFetch(request);

        assertThatThrownBy(() -> productApplicationService.saveProduct(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage(TestMessageUtil.getMessage(PRODUCT_VARIANT_SKU_CONFLICT));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(카테고리를 찾을 수 없음)")
    void saveProductTest_unit_notFound_category(){
        ProductRequest request = createProductRequest();

        //요청 Body 검증 통과
        doNothing().when(requestValidator).validateProductRequest(request);

        //카테고리 없음 예외를 던짐
        doThrow(new NotFoundException(TestMessageUtil.getMessage(CATEGORY_NOT_FOUND)))
                .when(productReferentialService).validAndFetch(request);

        assertThatThrownBy(() -> productApplicationService.saveProduct(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(TestMessageUtil.getMessage(CATEGORY_NOT_FOUND));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(카테고리가 최하위 카테고리가 아님)")
    void saveProductTest_unit_category_not_leaf(){
        ProductRequest request = createProductRequest();

        //요청 Body 검증 통과
        doNothing().when(requestValidator).validateProductRequest(request);

        //카테고리 검증 예외를 던짐
        doThrow(new BadRequestException(TestMessageUtil.getMessage(PRODUCT_CATEGORY_BAD_REQUEST)))
                .when(productReferentialService).validAndFetch(request);

        assertThatThrownBy(() -> productApplicationService.saveProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(TestMessageUtil.getMessage(PRODUCT_CATEGORY_BAD_REQUEST));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(옵션 타입을 찾을 수 없음)")
    void saveProductTest_unit_notFound_optionType(){
        ProductRequest request = createProductRequest();

        //요청 Body 검증 통과
        doNothing().when(requestValidator).validateProductRequest(request);

        //옵션 타입 없음 예외를 던짐
        doThrow(new NotFoundException(TestMessageUtil.getMessage(OPTION_TYPE_NOT_FOUND)))
                .when(productReferentialService).validAndFetch(request);

        assertThatThrownBy(() -> productApplicationService.saveProduct(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(TestMessageUtil.getMessage(OPTION_TYPE_NOT_FOUND));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(옵션 값을 찾을 수 없음)")
    void saveProductTest_unit_notFound_optionValue(){
        ProductRequest request = createProductRequest();

        //요청 Body 검증 통과
        doNothing().when(requestValidator).validateProductRequest(request);

        //옵션 값 없음 예외를 던짐
        doThrow(new NotFoundException(TestMessageUtil.getMessage(OPTION_VALUE_NOT_FOUND)))
                .when(productReferentialService).validAndFetch(request);

        assertThatThrownBy(() -> productApplicationService.saveProduct(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(TestMessageUtil.getMessage(OPTION_VALUE_NOT_FOUND));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(상품 Variant의 OptionValue가 상품 OptionType의 하위 객체가 아닌 경우)")
    void saveProductTest_unit_optionValue_notMatch_optionType(){
        Categories category = createCategory(1L, "핸드폰", "http://phone.jpg");
        OptionTypes storage = createOptionType(1L, "용량");
        OptionTypes color = createOptionType(2L, "색상");
        OptionValues gb_128 = createOptionValue(1L, "128GB", storage);
        OptionValues XL = createOptionValue(3L, "XL", createOptionType(3L, "사이즈"));


        Map<Long, OptionTypes> optionTypeById = createOptionTypeMap(storage, color);
        Map<Long, OptionValues> optionValueById = createOptionValueMap(gb_128, XL);
        ProductCreationData creationData = new ProductCreationData(category, optionTypeById, optionValueById);

        ProductRequest request = createProductRequest();
        request.setProductVariants(
                List.of(new ProductVariantRequest("아이폰 16-2", 1200000, 100, 5,
                        List.of(new VariantOptionValueRequest(1L, 1L),
                                new VariantOptionValueRequest(2L, 3L))))
        );

        //요청 Body 검증 통과
        doNothing().when(requestValidator).validateProductRequest(request);

        //상품 생성 데이터 조회 성공
        when(productReferentialService.validAndFetch(request))
                .thenReturn(creationData);

        //옵션값이 optionType의 연관 엔티티가 아님 예외를 던짐
        doThrow(new BadRequestException(TestMessageUtil.getMessage(PRODUCT_OPTION_VALUE_NOT_MATCH_TYPE)))
                .when(factory).createProducts(request, creationData);

        assertThatThrownBy(() -> productApplicationService.saveProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(TestMessageUtil.getMessage(PRODUCT_OPTION_VALUE_NOT_MATCH_TYPE));
    }

    private ProductRequest createProductRequest(){
        return new ProductRequest("아이폰 16 Pro", "애플 IPhone 16 (Model-Pro)", 1L,
                List.of(new ImageRequest("http://iphone16-1.jpg"), new ImageRequest("http://iphone16-2.jpg")),
                List.of(new ProductOptionTypeRequest(1L, 0), new ProductOptionTypeRequest(2L, 1)),
                List.of(
                        new ProductVariantRequest("IPHONE16Pro-128GB-RED", 1200000, 100, 5,
                                List.of(new VariantOptionValueRequest(1L, 1L),
                                        new VariantOptionValueRequest(2L, 2L)))
                )
        );
    }

    private Products createProduct(ProductRequest request, Categories categories, ProductCreationData data){
        Products product = new Products("아이폰 16 Pro", "애플 IPhone 16 (Model-Pro)", categories);

        List<ProductImages> productImages = new ArrayList<>();
        List<ProductOptionTypes> productOptionTypes = new ArrayList<>();
        List<ProductVariants> productVariants = new ArrayList<>();

        for(ProductOptionTypeRequest otr : request.getProductOptionTypes()){
            ProductOptionTypes productOptionType = new ProductOptionTypes(data.getOptionTypeById().get(otr.getOptionTypeId()), otr.getPriority(), true);
            productOptionTypes.add(productOptionType);
        }

        for(ImageRequest imageRequest : request.getImages()){
            ProductImages productImage = new ProductImages(imageRequest.getUrl());
            productImages.add(productImage);
        }

        for(ProductVariantRequest pvr : request.getProductVariants()){
            ProductVariants productVariant =
                    new ProductVariants(pvr.getSku(), pvr.getPrice(), pvr.getStockQuantity(), pvr.getDiscountRate());

            for(VariantOptionValueRequest ovr : pvr.getVariantOption()){
                ProductVariantOptions productVariantOption =
                        new ProductVariantOptions(data.getOptionValueById().get(ovr.getOptionValueId()));
                productVariant.addProductVariantOption(productVariantOption);
            }
            productVariants.add(productVariant);
        }

        product.addImages(productImages);
        product.addOptionTypes(productOptionTypes);
        product.addVariants(productVariants);
        return product;
    }

    private Map<Long, OptionTypes> createOptionTypeMap(OptionTypes... optionTypes){
        Map<Long, OptionTypes> optionTypesMap = new HashMap<>();
        for (OptionTypes optionType : optionTypes) {
            optionTypesMap.put(optionType.getId(), optionType);
        }
        return optionTypesMap;
    }

    private Map<Long, OptionValues> createOptionValueMap(OptionValues... optionValues){
        Map<Long, OptionValues> optionValuesMap = new HashMap<>();
        for (OptionValues optionValue : optionValues) {
            optionValuesMap.put(optionValue.getId(), optionValue);
        }
        return optionValuesMap;
    }

    private Categories createCategory(Long id, String name, String iconUrl){
        Categories categories = new Categories(name, iconUrl);
        ReflectionTestUtils.setField(categories, "id", id);
        return categories;
    }

    private OptionTypes createOptionType(Long id, String name){
        OptionTypes optionTypes = new OptionTypes(name);
        ReflectionTestUtils.setField(optionTypes, "id", id);
        return optionTypes;
    }

    private OptionValues createOptionValue(Long id, String optionValue, OptionTypes optionTypes){
        OptionValues optionValues = new OptionValues(optionValue);
        ReflectionTestUtils.setField(optionValues, "id", id);
        optionTypes.addOptionValue(optionValues);
        return optionValues;
    }

}
