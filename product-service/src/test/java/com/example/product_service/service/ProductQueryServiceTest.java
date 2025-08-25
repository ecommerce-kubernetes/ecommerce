package com.example.product_service.service;

import com.example.product_service.dto.ProductSearch;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.ReviewResponse;
import com.example.product_service.dto.response.product.ProductResponse;
import com.example.product_service.dto.response.product.ProductSummaryResponse;
import com.example.product_service.dto.response.variant.ProductVariantResponse;
import com.example.product_service.entity.*;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.CategoryRepository;
import com.example.product_service.repository.OptionTypeRepository;
import com.example.product_service.repository.ProductsRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static com.example.product_service.common.MessagePath.PRODUCT_NOT_FOUND;
import static com.example.product_service.util.TestMessageUtil.getMessage;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("mysql")
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ProductQueryServiceTest {
    @Autowired
    ProductsRepository productsRepository;
    @Autowired
    OptionTypeRepository optionTypeRepository;
    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    ProductQueryService productQueryService;

    @Autowired
    EntityManager em;
    OptionTypes storage;
    OptionValues gb_128;
    Categories electronic;
    @BeforeEach
    void saveFixture() {
        storage = new OptionTypes("용량");
        gb_128 = new OptionValues("128GB");
        electronic = new Categories("전자 기기", "http://electronic.jpg");
        storage.addOptionValue(gb_128);
        optionTypeRepository.save(storage);
        categoryRepository.save(electronic);
    }

    @AfterEach
    void clearDB(){
        productsRepository.deleteAll();
        categoryRepository.deleteAll();
        optionTypeRepository.deleteAll();
    }

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0.33")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    }

    @Test
    @DisplayName("상품 목록 조회 테스트-성공")
    @Transactional
    void getProductsTest_integration_success(){
        ProductImages productImage = createProductImages("http://test.jpg", 0);
        ProductOptionTypes productOptionType = createProductOptionType(storage);
        ProductVariants productVariant = createProductVariants("sku", 10000, 100, 10, gb_128);

        Products product = createProduct("productName", "description", electronic,
                List.of(productImage), List.of(productOptionType), List.of(productVariant));

        Products savedProduct = productsRepository.save(product);
        em.flush(); em.clear();

        ProductSearch productSearch = new ProductSearch(electronic.getId(), "", null);
        Pageable pageable = PageRequest.of(0, 10);
        PageDto<ProductSummaryResponse> response = productQueryService.getProducts(productSearch, pageable);

        assertThat(response.getPageSize()).isEqualTo(10);
        assertThat(response.getCurrentPage()).isEqualTo(0);
        assertThat(response.getTotalPage()).isEqualTo(1);
        assertThat(response.getTotalElement()).isEqualTo(1);
        assertThat(response.getContent()).extracting("id", "name", "description",
                        "thumbnail", "categoryId", "ratingAvg", "reviewCount", "minimumPrice", "discountPrice", "discountRate")
                .containsExactlyInAnyOrder(
                        tuple(
                                savedProduct.getId(), savedProduct.getName(), savedProduct.getDescription(),
                                "http://test.jpg", savedProduct.getCategory().getId(), 0.0, 0, 10000, 9000, 10
                        )
                );
    }

    @Test
    @DisplayName("상품 상세 정보 조회 테스트-성공")
    @Transactional
    void getProductByIdTest_integration_success(){
        ProductImages productImage = createProductImages("http://test.jpg", 0);
        ProductOptionTypes productOptionType = createProductOptionType(storage);
        ProductVariants productVariant = createProductVariants("sku", 10000, 100, 10, gb_128);

        Products product = createProduct("productName", "description", electronic,
                List.of(productImage), List.of(productOptionType), List.of(productVariant));

        Products savedProduct = productsRepository.save(product);
        em.flush(); em.clear();

        ProductResponse response = productQueryService.getProductById(savedProduct.getId());

        assertThat(response.getId()).isEqualTo(savedProduct.getId());
        assertThat(response.getName()).isEqualTo(savedProduct.getName());
        assertThat(response.getDescription()).isEqualTo(savedProduct.getDescription());
        assertThat(response.getReviewCount()).isEqualTo(0);
        assertThat(response.getAvgRating()).isEqualTo(0);
        assertThat(response.getImages()).extracting("id", "url", "sortOrder")
                .containsExactlyInAnyOrder(
                        tuple(productImage.getId(), productImage.getImageUrl(), productImage.getSortOrder())
                );

        assertThat(response.getProductOptionTypes())
                .extracting("id", "name")
                .containsExactlyInAnyOrder(
                        tuple(storage.getId(), storage.getName())
                );
        assertThat(response.getProductVariants())
                .extracting("sku", "price", "stockQuantity", "discountRate")
                .containsExactlyInAnyOrder(
                        tuple(productVariant.getSku(), productVariant.getPrice(), productVariant.getStockQuantity(),
                                productVariant.getDiscountValue())
                );

        assertThat(response.getProductVariants())
                .flatExtracting(ProductVariantResponse::getOptionValues)
                .extracting("valueId", "typeId", "valueName")
                .containsExactlyInAnyOrder(
                        tuple(gb_128.getId(), storage.getId(), gb_128.getOptionValue())
                );
    }

    @Test
    @DisplayName("상품 상세 조회 테스트-실패(상품을 찾을 수 없음)")
    void getProductByIdTest_integration_notFound(){
        assertThatThrownBy(() -> productQueryService.getProductById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(PRODUCT_NOT_FOUND));
    }

    @Test
    @DisplayName("인기 상품 조회 테스트-성공")
    @Transactional
    void getPopularProductsTest_integration_success(){
        ProductImages product1Image = createProductImages("http://test.jpg", 0);
        ProductOptionTypes product1OptionType = createProductOptionType(storage);
        ProductVariants product1Variant = createProductVariants("sku1", 10000, 100, 10, gb_128);
        for(long i=1; i<6; i++){
            product1Variant.addReview(i, "user"+i, 4, "good", List.of());
        }

        ProductImages product2Image = createProductImages("http://test.jpg", 0);
        ProductOptionTypes product2OptionType = createProductOptionType(storage);
        ProductVariants product2Variant = createProductVariants("sku2", 10000, 100, 10, gb_128);

        Products product1 = createProduct("productName", "description", electronic,
                List.of(product1Image), List.of(product1OptionType), List.of(product1Variant));
        Products product2 = createProduct("productName", "description", electronic,
                List.of(product2Image), List.of(product2OptionType), List.of(product2Variant));

        productsRepository.saveAll(List.of(product1, product2));
        em.flush(); em.clear();

        PageDto<ProductSummaryResponse> response = productQueryService.getPopularProducts(0, 10, null);

        assertThat(response.getPageSize()).isEqualTo(10);
        assertThat(response.getCurrentPage()).isEqualTo(0);
        assertThat(response.getTotalPage()).isEqualTo(1);
        assertThat(response.getTotalElement()).isEqualTo(1);
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent()).extracting("id", "name", "description",
                        "thumbnail", "categoryId", "ratingAvg", "reviewCount", "minimumPrice", "discountPrice", "discountRate")
                .containsExactlyInAnyOrder(
                        tuple(
                                product1.getId(), product1.getName(), product1.getDescription(),
                                "http://test.jpg", product1.getCategory().getId(), 4.0, 5, 10000, 9000, 10
                        )
                );
    }

    @Test
    @DisplayName("상품 리뷰 목록 조회 테스트-성공")
    @Transactional
    void getReviewsByProductIdTest_integration_success(){
        ProductImages productImage = createProductImages("http://test.jpg", 0);
        ProductOptionTypes productOptionType = createProductOptionType(storage);
        ProductVariants product1Variant = createProductVariants("sku1", 10000, 100, 10, gb_128);
        for(long i=1; i<6; i++){
            product1Variant.addReview(i, "user"+i, 4, "good", List.of());
        }

        Products product = createProduct("productName", "description", electronic,
                List.of(productImage), List.of(productOptionType), List.of(product1Variant));

        productsRepository.save(product);
        em.flush(); em.clear();

        Pageable pageable = PageRequest.of(0, 10, Sort.Direction.DESC, "createAt");

        PageDto<ReviewResponse> response = productQueryService.getReviewsByProductId(product.getId(), pageable);

        assertThat(response.getCurrentPage()).isEqualTo(0);
        assertThat(response.getPageSize()).isEqualTo(10);
        assertThat(response.getTotalPage()).isEqualTo(1);
        assertThat(response.getTotalElement()).isEqualTo(5);

        assertThat(response.getContent()).extracting("productName", "userId", "userName", "rating", "content")
                .containsExactlyInAnyOrder(
                        tuple("productName", 1L, "user1", 4, "good"),
                        tuple("productName", 2L, "user2", 4, "good"),
                        tuple("productName", 3L, "user3", 4, "good"),
                        tuple("productName", 4L, "user4", 4, "good"),
                        tuple("productName", 5L, "user5", 4, "good")
                );

        assertThat(response.getContent())
                .flatExtracting(ReviewResponse::getOptionValues)
                .extracting("valueId", "typeId", "valueName")
                .containsExactlyInAnyOrder(
                        tuple(gb_128.getId(), storage.getId(), gb_128.getOptionValue()),
                        tuple(gb_128.getId(), storage.getId(), gb_128.getOptionValue()),
                        tuple(gb_128.getId(), storage.getId(), gb_128.getOptionValue()),
                        tuple(gb_128.getId(), storage.getId(), gb_128.getOptionValue()),
                        tuple(gb_128.getId(), storage.getId(), gb_128.getOptionValue())
                );
    }

    @Test
    @DisplayName("상품 리뷰 목록 조회 테스트-실패(상품을 찾을 수 없음)")
    void getReviewsByProductIdTest_integration_notFound(){
        assertThatThrownBy(() -> productQueryService.getReviewsByProductId(999L, PageRequest.of(0, 10)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(PRODUCT_NOT_FOUND));
    }

    private ProductImages createProductImages(String imageUrl, int sortOrder){
        return new ProductImages(imageUrl, sortOrder);
    }

    private ProductOptionTypes createProductOptionType(OptionTypes optionTypes){
        return new ProductOptionTypes(optionTypes, 0, true);
    }

    private ProductVariants createProductVariants(String sku, int price, int stockQuantity, int discountValue, OptionValues optionValues){
        ProductVariantOptions productVariantOptions = new ProductVariantOptions(optionValues);

        ProductVariants productVariants = new ProductVariants(sku, price, stockQuantity, discountValue);
        productVariants.addProductVariantOption(productVariantOptions);
        return productVariants;
    }

    private Products createProduct(String name, String description, Categories category,
                                   List<ProductImages> productImages, List<ProductOptionTypes> productOptionTypes,
                                   List<ProductVariants> productVariants){
        Products product = new Products(name, description, category);
        product.addImages(productImages);
        product.addOptionTypes(productOptionTypes);
        product.addVariants(productVariants);

        return product;
    }
}
