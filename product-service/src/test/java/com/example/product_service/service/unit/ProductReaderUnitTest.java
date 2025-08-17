package com.example.product_service.service.unit;

import com.example.product_service.dto.ProductSearch;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.product.ProductSummaryResponse;
import com.example.product_service.entity.DomainType;
import com.example.product_service.entity.ProductSummary;
import com.example.product_service.repository.CategoryRepository;
import com.example.product_service.repository.ProductSummaryRepository;
import com.example.product_service.service.ProductReader;
import com.example.product_service.service.util.validator.PageableValidatorFactory;
import com.example.product_service.service.util.validator.ProductPageableValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductReaderUnitTest {

    @Mock
    CategoryRepository categoryRepository;
    @Mock
    ProductSummaryRepository productSummaryRepository;
    @Mock
    PageableValidatorFactory pageableValidatorFactory;

    @InjectMocks
    ProductReader productReader;

    @Test
    @DisplayName("상품 조회 테스트-성공")
    void getProductsTest_unit_success(){
        ProductSearch productSearch = new ProductSearch(2L, "", 2);
        mockCategoryFindDescendantIds(2L, List.of(2L, 3L));
        Pageable pageable = PageRequest.of(0, 10, Sort.Direction.ASC, "rating");
        Page<ProductSummary> pageProductSummary = createPageProductSummary(pageable);
        mockProductSummaryFindAll("", List.of(2L, 3L), 2, pageable, pageProductSummary);
        mockPageableValidatorFactory();
        PageDto<ProductSummaryResponse> products = productReader.getProducts(productSearch, pageable);

        assertThat(products.getPageSize()).isEqualTo(10);
        assertThat(products.getCurrentPage()).isEqualTo(0);
        List<ProductSummaryResponse> content = products.getContent();
        assertThat(content)
                .extracting("name", "description", "categoryId", "thumbnail", "ratingAvg", "reviewCount",
                        "minimumPrice", "discountPrice", "discountRate")
                .containsExactlyInAnyOrder(
                        tuple("productName", "description", 2L, "http://test.jpg", 3.5, 10,
                                1000, 900, 10)
                );
    }

    private void mockCategoryFindDescendantIds(Long categoryId, List<Long> returnResult){
        when(categoryRepository.findDescendantIds(categoryId)).thenReturn(returnResult);
    }

    private void mockProductSummaryFindAll(String name, List<Long> categoryIds, Integer rating, Pageable pageable, Page<ProductSummary> returnResult){
        when(productSummaryRepository.findAllProductSummary(name, categoryIds, rating, pageable))
                .thenReturn(returnResult);
    }

    private Page<ProductSummary> createPageProductSummary(Pageable pageable){
        List<ProductSummary> content = List.of(new ProductSummary(
                1L, "productName", "description", 2L, "http://test.jpg", 3.5,
                10, 1000, 900, 10, LocalDateTime.now()

        ));
        return new PageImpl<>(content, pageable, 1);
    }

    private void mockPageableValidatorFactory(){
        when(pageableValidatorFactory.getValidator(DomainType.PRODUCT))
                .thenReturn(new ProductPageableValidator());
    }
}
