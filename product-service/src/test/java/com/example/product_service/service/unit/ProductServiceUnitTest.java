package com.example.product_service.service.unit;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.controller.util.MessagePath;
import com.example.product_service.controller.util.TestMessageUtil;
import com.example.product_service.dto.request.image.ImageRequest;
import com.example.product_service.dto.request.options.ProductOptionTypeRequest;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
import com.example.product_service.dto.request.variant.VariantOptionValueRequest;
import com.example.product_service.dto.response.product.ProductResponse;
import com.example.product_service.entity.Categories;
import com.example.product_service.entity.OptionTypes;
import com.example.product_service.entity.OptionValues;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.CategoryRepository;
import com.example.product_service.repository.OptionTypeRepository;
import com.example.product_service.repository.ProductsRepository;
import com.example.product_service.service.ProductService;
import org.assertj.core.api.Assertions;
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

import static com.example.product_service.controller.util.MessagePath.*;
import static com.example.product_service.controller.util.TestMessageUtil.*;
import static org.assertj.core.api.Assertions.*;
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
                        new VariantOptionValueRequest(1L, 1L)
                )))
        );

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
                        tuple(1L, "type")
                );
        //TODO productVariant
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
        mockFindOptionTypeById(1L, null);
        mockMessageUtil(OPTION_TYPE_NOT_FOUND, "OptionType not found");
        assertThatThrownBy(() -> productService.saveProduct(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(OPTION_TYPE_NOT_FOUND));
    }


    private void mockMessageUtil(String code, String returnMessage){
        when(ms.getMessage(code)).thenReturn(returnMessage);
    }

    private void mockFindCategoryById(Long id, Categories o){
        OngoingStubbing<Optional<Categories>> when = when(categoryRepository.findById(id));
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
}
