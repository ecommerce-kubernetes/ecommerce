package com.example.product_service.service.unit;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.request.image.ImageRequest;
import com.example.product_service.dto.request.options.ProductOptionTypeRequest;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
import com.example.product_service.dto.request.variant.VariantOptionValueRequest;
import com.example.product_service.dto.response.product.ProductResponse;
import com.example.product_service.entity.Categories;
import com.example.product_service.entity.OptionTypes;
import com.example.product_service.entity.OptionValues;
import com.example.product_service.entity.Products;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.exception.DuplicateResourceException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.*;
import com.example.product_service.service.ProductService;
import com.example.product_service.service.util.ProductRequestStructureValidator;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static com.example.product_service.common.MessagePath.*;
import static com.example.product_service.util.TestMessageUtil.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductServiceUnitTest {
    @Mock
    ProductsRepository productsRepository;
    @Mock
    CategoryRepository categoryRepository;
    @Mock
    OptionTypeRepository optionTypeRepository;
    @Mock
    ProductVariantsRepository productVariantsRepository;
    @Mock
    OptionValueRepository optionValueRepository;
    @Mock
    ProductRequestStructureValidator productRequestStructureValidator;
    @Mock
    MessageSourceUtil ms;

    @InjectMocks
    ProductService productService;

    @Test
    @DisplayName("상품 저장 테스트-성공")
    void saveProductTest_unit_success(){
        ProductRequest request = new ProductRequest(
                "name",
                "description",
                1L,
                List.of(new ImageRequest("http://test.jpg", 0)),
                List.of(new ProductOptionTypeRequest(1L, 1)),
                List.of(new ProductVariantRequest("sku", 3000, 100, 10, List.of(
                        new VariantOptionValueRequest(1L, 5L)
                )))
        );
        Categories category = createCategoriesWithSetId(1L, "category", "http://category.jpg");
        OptionTypes optionType = createOptionTypesWithSetId(1L, "optionType");
        OptionValues optionValue = createOptionValuesWithSetId(5L, "optionValue");
        optionType.addOptionValue(optionValue);
        doNothing().when(productRequestStructureValidator).validateProductRequest(any());
        mockFindCategoryById(1L, category);
        mockFindOptionTypeByIdInOrThrow(List.of(1L), List.of(optionType));
        mockFindOptionValueById(5L, optionValue);
        mockExistsSku("sku", false);
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
    @DisplayName("상품 저장 테스트-실패(카테고리 없음)")
    void saveProductTest_unit_category_notFound(){
        ProductRequest request = new ProductRequest(
                "name",
                "description",
                1L,
                List.of(new ImageRequest("http://test.jpg", 0)),
                List.of(new ProductOptionTypeRequest(1L, 1)),
                List.of(new ProductVariantRequest("sku", 3000, 100, 10, List.of(
                        new VariantOptionValueRequest(1L, 1L)
                )))
        );
        mockFindCategoryById(1L, null);
        mockMessageUtil(CATEGORY_NOT_FOUND, "Category not found");
        assertThatThrownBy(() -> productService.saveProduct(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(CATEGORY_NOT_FOUND));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(옵션 타입 없음)")
    void saveProductTest_unit_optionType_notFound(){
        ProductRequest request = new ProductRequest(
                "name",
                "description",
                1L,
                List.of(new ImageRequest("http://test.jpg", 0)),
                List.of(new ProductOptionTypeRequest(1L, 1)),
                List.of(new ProductVariantRequest("sku", 3000, 100, 10, List.of(
                        new VariantOptionValueRequest(1L, 1L)
                )))
        );
        Categories category = createCategoriesWithSetId(1L, "category", "http://category.jpg");
        mockFindCategoryById(1L, category);
        mockFindOptionTypeByIdInOrThrow(List.of(1L), List.of());
        mockMessageUtil(OPTION_TYPE_NOT_FOUND, "OptionType not found");
        assertThatThrownBy(() -> productService.saveProduct(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(OPTION_TYPE_NOT_FOUND));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(상품 변형 옵션중 상품 옵션과 다른것이 있을때)")
    void saveProductTest_unit_variantOptionValue_cardinality_violation1(){
        ProductRequest request = new ProductRequest(
                "name",
                "description",
                1L,
                List.of(new ImageRequest("http://test.jpg", 0)),
                List.of(new ProductOptionTypeRequest(1L, 1)),
                List.of(new ProductVariantRequest("sku", 3000, 100, 10,
                        List.of(new VariantOptionValueRequest(2L, 5L)
                )))
        );
        Categories category = createCategoriesWithSetId(1L, "category", "http://category.jpg");
        OptionTypes optionType = createOptionTypesWithSetId(1L, "optionType");
        mockFindCategoryById(1L, category);
        mockFindOptionTypeByIdInOrThrow(List.of(1L), List.of(optionType));
        mockMessageUtil(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION, "Each product variant must have exactly one option value per option type");

        assertThatThrownBy(() -> productService.saveProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION));
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
        Categories category = createCategoriesWithSetId(1L, "category", "http://category.jpg");
        OptionTypes optionType = createOptionTypesWithSetId(1L, "optionType");
        mockFindCategoryById(1L, category);
        mockFindOptionTypeByIdInOrThrow(List.of(1L), List.of(optionType));
        mockMessageUtil(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION, "Each product variant must have exactly one option value per option type");

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
        Categories category = createCategoriesWithSetId(1L, "category", "http://category.jpg");
        OptionTypes optionType1 = createOptionTypesWithSetId(1L, "optionType1");
        OptionTypes optionType2 = createOptionTypesWithSetId(2L, "optionType2");
        mockFindCategoryById(1L, category);
        mockFindOptionTypeByIdInOrThrow(List.of(1L, 2L), List.of(optionType1, optionType2));
        mockMessageUtil(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION, "Each product variant must have exactly one option value per option type");

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
        Categories category = createCategoriesWithSetId(1L, "category", "http://category.jpg");
        OptionTypes optionType1 = createOptionTypesWithSetId(1L, "optionType1");
        OptionTypes optionType2 = createOptionTypesWithSetId(2L, "optionType2");
        mockFindCategoryById(1L, category);

        mockFindOptionTypeByIdInOrThrow(List.of(1L,2L), List.of(optionType1, optionType2));
        mockMessageUtil(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION, "Each product variant must have exactly one option value per option type");

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
        Categories category = createCategoriesWithSetId(1L, "category", "http://category.jpg");
        OptionTypes optionType = createOptionTypesWithSetId(1L, "optionType");
        mockFindCategoryById(1L, category);
        mockFindOptionTypeByIdInOrThrow(List.of(1L), List.of(optionType));
        mockMessageUtil(PRODUCT_OPTION_VALUE_NOT_MATCH_TYPE, "OptionValue must belong to the OptionType");

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
        Categories category = createCategoriesWithSetId(1L, "category", "http://category.jpg");
        OptionTypes optionType = createOptionTypesWithSetId(1L, "optionType");
        OptionValues optionValue = createOptionValuesWithSetId(5L, "optionValue");
        optionType.addOptionValue(optionValue);
        mockFindCategoryById(1L, category);
        mockFindOptionTypeByIdInOrThrow(List.of(1L), List.of(optionType));
        mockExistsSku("sku", true);
        mockMessageUtil(PRODUCT_VARIANT_SKU_CONFLICT, "Product Variant SKU already exists");
        assertThatThrownBy(() -> productService.saveProduct(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage(getMessage(PRODUCT_VARIANT_SKU_CONFLICT));
    }

    private void mockMessageUtil(String code, String returnMessage){
        when(ms.getMessage(code)).thenReturn(returnMessage);
    }

    private void mockExistsSku(String sku, boolean isExist){
        OngoingStubbing<Boolean> when = when(productVariantsRepository.existsBySku(sku));
        if(isExist){
            when.thenReturn(true);
        } else {
            when.thenReturn(false);
        }
    }

    private void mockFindCategoryById(Long id, Categories o){
        OngoingStubbing<Optional<Categories>> when = when(categoryRepository.findById(id));
        if(o == null){
            when.thenReturn(Optional.empty());
        } else {
            when.thenReturn(Optional.of(o));
        }
    }

    private void mockFindOptionValueById(Long id, OptionValues o){
        OngoingStubbing<Optional<OptionValues>> when = when(optionValueRepository.findById(id));
        if(o == null){
            when.thenReturn(Optional.empty());
        } else {
            when.thenReturn(Optional.of(o));
        }
    }

    private void mockFindOptionTypeById(Long id, OptionTypes o){
        OngoingStubbing<Optional<OptionTypes>> when = when(optionTypeRepository.findById(id));
        if(o == null){
            when.thenReturn(Optional.empty());
        } else {
            when.thenReturn(Optional.of(o));
        }
    }

    private void mockFindOptionTypeByIdInOrThrow(List<Long> ids, List<OptionTypes> returnList) {
        when(optionTypeRepository.findByIdIn(ids)).thenReturn(returnList);
    }

    private Categories createCategoriesWithSetId(Long id, String name, String url){
        Categories categories = new Categories(name, url);
        ReflectionTestUtils.setField(categories, "id", id);
        return categories;
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
}
