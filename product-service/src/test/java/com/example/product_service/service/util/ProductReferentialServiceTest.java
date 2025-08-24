package com.example.product_service.service.util;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.request.image.ImageRequest;
import com.example.product_service.dto.request.options.ProductOptionTypeRequest;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
import com.example.product_service.dto.request.variant.VariantOptionValueRequest;
import com.example.product_service.entity.Categories;
import com.example.product_service.entity.OptionTypes;
import com.example.product_service.entity.OptionValues;
import com.example.product_service.entity.Products;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.exception.DuplicateResourceException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.CategoryRepository;
import com.example.product_service.repository.OptionTypeRepository;
import com.example.product_service.repository.OptionValueRepository;
import com.example.product_service.repository.ProductVariantsRepository;
import com.example.product_service.service.dto.ProductCreationData;
import com.example.product_service.service.util.validator.ProductReferentialService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.example.product_service.common.MessagePath.*;
import static com.example.product_service.util.TestMessageUtil.getMessage;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductReferentialServiceTest {
    @Mock
    CategoryRepository categoryRepository;
    @Mock
    OptionTypeRepository optionTypeRepository;
    @Mock
    ProductVariantsRepository productVariantsRepository;
    @Mock
    OptionValueRepository optionValueRepository;
    @Mock
    MessageSourceUtil ms;

    @InjectMocks
    ProductReferentialService validator;

    @Test
    @DisplayName("ProductRequest-성공")
    void validAndFetch_success(){
        Categories category = createCategoriesWithSetId(1L, "category", "http://test.jpg");
        mockExistsBySkuIn(false, "sku");
        mockFindByCategory(1L, category);

        OptionTypes optionType = createOptionTypesWithSetId(1L, "optionType");
        OptionValues optionValue = createOptionValuesWithSetId(1L, "optionValue");
        optionType.addOptionValue(optionValue);

        mockFindOptionTypeIn(List.of(1L), List.of(optionType));
        mockFindOptionValueIn(List.of(1L), List.of(optionValue));
        ProductRequest request = createProductRequest();

        ProductCreationData result = validator.validAndFetch(request);

        assertThat(result.getCategory()).isEqualTo(category);
        assertThat(result.getOptionTypeById()).hasSize(1);
        assertThat(result.getOptionTypeById().get(optionType.getId())).isEqualTo(optionType);
        assertThat(result.getOptionValueById().get(optionValue.getId())).isEqualTo(optionValue);
    }

    @Test
    @DisplayName("ProductRequest SKU 중복")
    void validAndFetch_duplicate_sku(){
        mockExistsBySkuIn(true, "sku");
        mockMessageUtil(PRODUCT_VARIANT_SKU_CONFLICT, "Product Variant SKU Conflict");
        ProductRequest request = createProductRequest();
        assertThatThrownBy(() -> validator.validAndFetch(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage(getMessage(PRODUCT_VARIANT_SKU_CONFLICT));
    }

    @Test
    @DisplayName("ProductRequest 카테고리 찾을 수 없음")
    void validAndFetch_notFound_category(){
        mockExistsBySkuIn(false, "sku");
        mockFindByCategory(1L, null);
        mockMessageUtil(CATEGORY_NOT_FOUND, "Category not found");
        ProductRequest request = createProductRequest();
        assertThatThrownBy(() -> validator.validAndFetch(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(CATEGORY_NOT_FOUND));
    }

    @Test
    @DisplayName("ProductRequest 카테고리가 최하위 카테고리가 아닌경우")
    void validAndFetch_badRequest_category(){
        mockExistsBySkuIn(false, "sku");
        Categories root = createCategoriesWithSetId(1L, "root", "http://test.jpg");
        Categories leaf = createCategoriesWithSetId(2L, "leaf", null);
        root.addChild(leaf);
        mockFindByCategory(1L, root);
        mockMessageUtil(PRODUCT_CATEGORY_BAD_REQUEST, "category must be the lowest level");
        ProductRequest request = createProductRequest();
        assertThatThrownBy(() -> validator.validAndFetch(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_CATEGORY_BAD_REQUEST));
    }

    @Test
    @DisplayName("ProductRequest 옵션 타입 찾을 수 없음")
    void validAndFetch_notFound_optionType(){
        Categories category = createCategoriesWithSetId(1L, "category", "http://test.jpg");
        mockExistsBySkuIn(false, "sku");
        mockFindByCategory(1L, category);
        mockFindOptionTypeIn(List.of(1L), List.of());
        mockMessageUtil(OPTION_TYPE_NOT_FOUND, "OptionType not found");
        ProductRequest request = createProductRequest();
        assertThatThrownBy(() -> validator.validAndFetch(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(OPTION_TYPE_NOT_FOUND));
    }

    @Test
    @DisplayName("ProductRequest 옵션 값을 찾을 수 없음")
    void validAndFetch_notFound_optionValue(){
        Categories category = createCategoriesWithSetId(1L, "category", "http://test.jpg");
        OptionTypes optionType = createOptionTypesWithSetId(1L, "optionType");
        mockExistsBySkuIn(false, "sku");
        mockFindByCategory(1L, category);
        mockFindOptionTypeIn(List.of(1L), List.of(optionType));
        mockFindOptionValueIn(List.of(1L), List.of());
        mockMessageUtil(OPTION_VALUE_NOT_FOUND, "OptionValue not found");
        ProductRequest request = createProductRequest();
        assertThatThrownBy(() -> validator.validAndFetch(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(OPTION_VALUE_NOT_FOUND));
    }

    @Test
    @DisplayName("ProductVariantRequest-성공")
    void validateProductVariantTest_success(){
        ProductVariantRequest request = createProductVariantRequest();
    }

    @Test
    @DisplayName("ProductVariantRequest-sku 중복")
    void validateProductVariantTest_duplicate_sku(){
        mockExistsBySkuIn(true, "sku");
        mockMessageUtil(PRODUCT_VARIANT_SKU_CONFLICT, "Product Variant SKU Conflict");
        ProductVariantRequest request = createProductVariantRequest();
        assertThatThrownBy(() -> validator.validateProductVariant(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage(getMessage(PRODUCT_VARIANT_SKU_CONFLICT));
    }

    @Test
    @DisplayName("ProductVariantRequest-옵션 값 찾을 수 없음")
    void validateProductVariantTest_notFound_optionValue(){
        mockExistsBySkuIn(false, "sku");
        mockFindOptionValueIn(List.of(1L), List.of());
        mockMessageUtil(OPTION_VALUE_NOT_FOUND, "OptionValue not found");
        ProductVariantRequest request = createProductVariantRequest();
        assertThatThrownBy(() -> validator.validateProductVariant(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(OPTION_VALUE_NOT_FOUND));
    }


    private void mockExistsBySkuIn(boolean isExists, String... skus){
        List<String> skuList = Arrays.asList(skus);
        OngoingStubbing<Boolean> when = when(productVariantsRepository.existsBySkuIn(new ArrayList<>(skuList)));
        if(isExists){
            when.thenReturn(true);
        } else {
            when.thenReturn(false);
        }
    }

    private void mockFindByCategory(Long categoryId, Categories returnResult){
        OngoingStubbing<Optional<Categories>> when = when(categoryRepository.findById(categoryId));
        if(returnResult == null){
            when.thenReturn(Optional.empty());
        } else {
            when.thenReturn(Optional.of(returnResult));
        }
    }

    private void mockFindOptionTypeIn(List<Long> ids, List<OptionTypes> returnResult){
        when(optionTypeRepository.findByIdIn(ids)).thenReturn(returnResult);
    }

    private void mockFindOptionValueIn(List<Long> ids, List<OptionValues> returnResult){
        when(optionValueRepository.findByIdIn(ids)).thenReturn(returnResult);
    }

    private void mockMessageUtil(String code, String returnString){
        when(ms.getMessage(code)).thenReturn(returnString);
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

    private ProductRequest createProductRequest(){
        return new ProductRequest(
                "name",
                "description",
                1L,
                List.of(new ImageRequest("http://test.jpg")),
                List.of(new ProductOptionTypeRequest(1L, 1)),
                List.of(new ProductVariantRequest("sku", 1000, 100, 10,
                        List.of(
                                new VariantOptionValueRequest(1L, 1L)
                        )))

        );
    }

    private ProductVariantRequest createProductVariantRequest(){
        return new ProductVariantRequest(
                "sku", 10000, 100, 10,
                List.of(new VariantOptionValueRequest(1L, 1L))
        );
    }
}