package com.example.product_service.service.unit;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.ProductSearch;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.ReviewResponse;
import com.example.product_service.dto.response.product.ProductResponse;
import com.example.product_service.dto.response.product.ProductSummaryResponse;
import com.example.product_service.dto.response.variant.ProductVariantResponse;
import com.example.product_service.entity.*;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.*;
import com.example.product_service.service.ProductQueryService;
import com.example.product_service.service.dto.ReviewStats;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.example.product_service.common.MessagePath.PRODUCT_NOT_FOUND;
import static com.example.product_service.util.TestMessageUtil.getMessage;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductQueryServiceUnitTest {

    @Mock
    CategoryRepository categoryRepository;
    @Mock
    ProductSummaryRepository productSummaryRepository;
    @Mock
    ProductsRepository productsRepository;
    @Mock
    ProductImagesRepository productImagesRepository;
    @Mock
    ProductOptionTypesRepository productOptionTypesRepository;
    @Mock
    ProductVariantsRepository productVariantsRepository;
    @Mock
    ReviewsRepository reviewsRepository;
    @Mock
    MessageSourceUtil ms;
    @InjectMocks
    ProductQueryService productQueryService;

    @Test
    @DisplayName("상품 조회 테스트-성공")
    void getProductsTest_unit_success(){
        ProductSearch productSearch = new ProductSearch(2L, "", 2);
        mockCategoryFindDescendantIds(2L, List.of(2L, 3L));
        Pageable pageable = PageRequest.of(0, 10, Sort.Direction.ASC, "rating");
        Page<ProductSummary> pageProductSummary = createPageProductSummary(pageable);
        mockProductSummaryFindAll("", List.of(2L, 3L), 2, pageable, pageProductSummary);
        PageDto<ProductSummaryResponse> products = productQueryService.getProducts(productSearch, pageable);

        assertThat(products.getPageSize()).isEqualTo(10);
        assertThat(products.getCurrentPage()).isEqualTo(0);
        List<ProductSummaryResponse> content = products.getContent();
        assertThat(content)
                .extracting("name", "description", "categoryId", "thumbnail", "ratingAvg", "reviewCount",
                        "minimumPrice", "discountPrice", "discountRate")
                .containsExactly(
                        tuple("productName", "description", 2L, "http://test.jpg", 3.5, 10,
                                1000, 900, 10),
                        tuple("productName2", "description", 2L, "http://test.jpg", 3.2, 10,
                                10000, 10000, 0)
                );
    }

    @Test
    @DisplayName("상품 상세 조회 테스트-성공")
    void getProductByIdTest_unit_success(){
        Categories category = createCategory(1L, "category");
        OptionTypes optionType = new OptionTypes("name");
        OptionValues optionValue = new OptionValues("optionValue");
        optionType.addOptionValue(optionValue);
        Products product = createProduct(1L, "productName", "description", category);
        ProductVariants productVariant = new ProductVariants("sku", 10000, 100, 10);
        productVariant.addProductVariantOption(new ProductVariantOptions(optionValue));
        mockProductFindById(1L, product);
        mockProductImageByProductId(1L);
        mockProductOptionTypes(1L, optionType);
        mockProductVariant(1L, List.of(productVariant));
        mockReviewStats(1L, 30L, 2.6);

        ProductResponse response = productQueryService.getProductById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("productName");
        assertThat(response.getDescription()).isEqualTo("description");
        assertThat(response.getReviewCount()).isEqualTo(30);
        assertThat(response.getAvgRating()).isEqualTo(2.6);

        assertThat(response.getImages())
                .extracting("url", "sortOrder")
                .containsExactlyInAnyOrder(
                        tuple("http://test.jpg", 0)
                );

        assertThat(response.getProductOptionTypes())
                .extracting("name")
                .containsExactlyInAnyOrder("name");

        assertThat(response.getProductVariants())
                .extracting("sku", "price", "stockQuantity", "discountRate")
                .containsExactlyInAnyOrder(tuple("sku", 10000, 100, 10));

        assertThat(response.getProductVariants())
                .flatExtracting(ProductVariantResponse::getOptionValues)
                .extracting("valueName")
                .containsExactlyInAnyOrder(
                        "optionValue"
                );

    }

    @Test
    @DisplayName("상품 상세 조회 테스트-실패(상품 찾을 수 없음)")
    void getProductByIdTest_unit_notFound(){
        mockProductFindById(1L, null);
        mockMessageUtil(PRODUCT_NOT_FOUND, "Product not found");
        assertThatThrownBy(() -> productQueryService.getProductById(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(PRODUCT_NOT_FOUND));
    }

    @Test
    @DisplayName("인기 상품 조회 테스트-성공")
    void getPopularProductsTest_unit_success(){
        when(productSummaryRepository.findAvgRating())
                .thenReturn(3.2);
        mockCategoryFindDescendantIds(1L, List.of(1L, 2L));
        Pageable pageable = PageRequest.of(0, 10);
        when(productSummaryRepository.findPopularProductSummary(List.of(1L,2L), 3.2, 5, pageable))
                .thenReturn(createPageProductSummary(pageable));

        PageDto<ProductSummaryResponse> products = productQueryService.getPopularProducts(0, 10, 1L);

        assertThat(products.getPageSize()).isEqualTo(10);
        assertThat(products.getCurrentPage()).isEqualTo(0);
    }

    @Test
    @DisplayName("상품 리뷰 목록 조회 테스트-성공")
    void getReviewByProductId_unit_success(){
        Pageable pageable = PageRequest.of(0, 10, Sort.Direction.DESC, "createAt");
        when(productsRepository.existsById(1L)).thenReturn(true);
        when(reviewsRepository.findAllByProductId(1L, pageable))
                .thenReturn(createPageReviews(pageable));

        PageDto<ReviewResponse> response = productQueryService.getReviewsByProductId(1L, pageable);

        assertThat(response.getCurrentPage()).isEqualTo(0);
        assertThat(response.getPageSize()).isEqualTo(10);
        assertThat(response.getTotalPage()).isEqualTo(1);
        assertThat(response.getTotalElement()).isEqualTo(3);

        assertThat(response.getContent())
                .extracting( "productName", "userId", "userName", "rating", "content")
                .containsExactlyInAnyOrder(
                        tuple( "productName", 1L, "user1", 4, "very good"),
                        tuple("productName", 2L, "user2", 3, "good"),
                        tuple("productName", 3L, "user3", 5, "excellent")
                );
    }

    @Test
    @DisplayName("상품 리뷰 목록 조회 테스트-실패(상품 찾을 수 없음)")
    void getReviewsByProductId_unit_notFound(){
        Pageable pageable = PageRequest.of(0, 10, Sort.Direction.DESC, "createAt");
        when(productsRepository.existsById(1L))
                .thenReturn(false);
        mockMessageUtil(PRODUCT_NOT_FOUND, "Product not found");
        assertThatThrownBy(() -> productQueryService.getReviewsByProductId(1L, pageable))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(PRODUCT_NOT_FOUND));
    }

    private void mockProductFindById(Long productId, Products products){
        OngoingStubbing<Optional<Products>> when = when(productsRepository.findWithCategoryById(productId));
        if(products == null) {
            when.thenReturn(Optional.empty());
        } else {
            when.thenReturn(Optional.of(products));
        }
    }

    private void mockProductImageByProductId(Long productId){
        when(productImagesRepository.findByProductId(productId)).thenReturn(
                List.of(new ProductImages("http://test.jpg", 0))
        );
    }

    private void mockProductOptionTypes(Long productId, OptionTypes optionTypes){
        when(productOptionTypesRepository.findWithOptionTypeByProductId(productId))
                .thenReturn(List.of(new ProductOptionTypes(optionTypes, 0, true)));
    }

    private Products createProduct(Long id, String name, String description, Categories category){
        Products products = new Products(name, description, category);
        ReflectionTestUtils.setField(products, "id", id);
        return products;
    }

    private Categories createCategory(Long id, String name){
        Categories categories = new Categories(name, "http://test.jpg");
        ReflectionTestUtils.setField(categories, "id", id);
        return categories;
    }



    private void mockCategoryFindDescendantIds(Long categoryId, List<Long> returnResult){
        when(categoryRepository.findDescendantIds(categoryId)).thenReturn(returnResult);
    }

    private void mockProductSummaryFindAll(String name, List<Long> categoryIds, Integer rating, Pageable pageable, Page<ProductSummary> returnResult){
        when(productSummaryRepository.findAllProductSummary(name, categoryIds, rating, pageable))
                .thenReturn(returnResult);
    }

    private void mockProductVariant(Long productId, List<ProductVariants> productVariants){
        when(productVariantsRepository.findWithVariantOptionByProductId(productId))
                .thenReturn(productVariants);
    }

    private Page<ProductSummary> createPageProductSummary(Pageable pageable){
        List<ProductSummary> content = List.of(new ProductSummary(
                1L, "productName", "description", 2L, "http://test.jpg", 3.5,
                10, 1000, 900, 10, LocalDateTime.now()),
                new ProductSummary(2L, "productName2", "description", 2L, "http://test.jpg",
                        3.2, 10, 10000, 10000, 0, LocalDateTime.now())
        );
        return new PageImpl<>(content, pageable, 1);
    }

    private Page<Reviews> createPageReviews(Pageable pageable){
        Products product = new Products("productName", "description", new Categories("category", null));
        ProductVariants productVariant = new ProductVariants("sku", 100, 10, 10);
        List<Reviews> content = List.of(new Reviews(productVariant, 1L, "user1", 4, "very good"),
                new Reviews(productVariant, 2L, "user2", 3, "good"),
                new Reviews(productVariant, 3L, "user3", 5, "excellent"));
        product.addVariants(List.of(productVariant));
        return new PageImpl<>(content, pageable, 3);
    }

    private void mockMessageUtil(String code, String returnMessage){
        when(ms.getMessage(code)).thenReturn(returnMessage);
    }

    private void mockReviewStats(Long productId, Long reviewCount, Double avgRating){
        when(reviewsRepository.findReviewStatsByProductId(productId))
                .thenReturn(new ReviewStats(reviewCount, avgRating));
    }
}
