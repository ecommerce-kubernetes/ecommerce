package com.example.product_service.service;

import com.example.product_service.dto.ProductSearch;
import com.example.product_service.dto.request.image.ImageRequest;
import com.example.product_service.dto.request.options.ProductOptionTypeRequest;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.product.UpdateProductBasicRequest;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
import com.example.product_service.dto.request.variant.VariantOptionValueRequest;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.ReviewResponse;
import com.example.product_service.dto.response.product.ProductResponse;
import com.example.product_service.dto.response.product.ProductSummaryResponse;
import com.example.product_service.dto.response.product.ProductUpdateResponse;
import com.example.product_service.dto.response.variant.ProductVariantResponse;
import com.example.product_service.entity.*;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.exception.DuplicateResourceException;
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
import java.util.Optional;

import static com.example.product_service.common.MessagePath.*;
import static com.example.product_service.util.TestMessageUtil.getMessage;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("mysql")
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductServiceTest {
    @Autowired
    ProductsRepository productsRepository;
    @Autowired
    OptionTypeRepository optionTypeRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    ProductService productService;

    @Autowired
    EntityManager em;

    OptionTypes existType;
    OptionTypes newOptionType;
    OptionValues newOptionValue1;
    OptionValues newOptionValue2;
    OptionValues existValue;
    Categories category;
    @BeforeEach
    void saveFixture(){
        existType = new OptionTypes("optionType");
        newOptionType = new OptionTypes("newOptionType");
        existValue = new OptionValues("optionValue");
        newOptionValue1 = new OptionValues("newOptionValue1");
        newOptionValue2 = new OptionValues("newOptionValue2");
        category = new Categories("category", "http://test.jpg");
        existType.addOptionValue(existValue);
        newOptionType.addOptionValue(newOptionValue1);
        newOptionType.addOptionValue(newOptionValue2);
        optionTypeRepository.saveAll(List.of(existType, newOptionType));
        categoryRepository.save(category);
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
    @DisplayName("상품 저장 테스트-성공")
    @Transactional
    void saveProduct_integration_success(){
        ProductRequest request = createProductRequest();

        ProductResponse response = productService.saveProduct(request);

        assertThat(response)
                .extracting("name", "description", "categoryId")
                .containsExactly("productName", "product description", category.getId());

        assertThat(response.getImages())
                .extracting("url", "sortOrder")
                        .containsExactlyInAnyOrder(
                                tuple("http://test.jpg", 0)
                        );

        assertThat(response.getProductOptionTypes())
                .extracting("id", "name")
                .containsExactlyInAnyOrder(
                        tuple(existType.getId(), existType.getName())
                );

        assertThat(response.getProductVariants())
                .extracting("sku", "price", "stockQuantity", "discountRate")
                .containsExactlyInAnyOrder(
                        tuple("sku", 100, 10, 10)
                );

        ProductVariantResponse variant = response.getProductVariants().stream()
                .filter(v -> "sku".equals(v.getSku()))
                .findFirst()
                .orElseThrow();

        assertThat(variant.getOptionValues()).hasSize(1);
        assertThat(variant.getOptionValues())
                .extracting("valueId", "typeId", "valueName")
                .containsExactly(tuple(existValue.getId(), existType.getId(), existValue.getOptionValue()));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(요청 Body에 중복된 productOptionTypeId)")
    void saveProductTest_integration_option_type_typeId_badRequest(){
        ProductRequest request = createProductRequest();
        request.setProductOptionTypes(List.of(new ProductOptionTypeRequest(existType.getId(), 0),
                new ProductOptionTypeRequest(existType.getId(), 1)));

        assertThatThrownBy(() -> productService.saveProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_OPTION_TYPE_TYPE_BAD_REQUEST));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(요청 Body에 중복된 priority)")
    void saveProductTest_integration_option_type_priority_badRequest(){
        ProductRequest request = createProductRequest();
        request.setProductOptionTypes(List.of(new ProductOptionTypeRequest(existType.getId(), 0),
                new ProductOptionTypeRequest(newOptionType.getId(), 0)));

        assertThatThrownBy(() -> productService.saveProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_OPTION_TYPE_PRIORITY_BAD_REQUEST));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(요청 Body에 중복된 SKU)")
    void saveProductTest_integration_request_sku_duplicate(){
        ProductRequest request = createProductRequest();
        request.setProductOptionTypes(
                List.of(new ProductOptionTypeRequest(existType.getId(), 0),
                        new ProductOptionTypeRequest(newOptionType.getId(), 1))
        );
        request.setProductVariants(
                List.of(new ProductVariantRequest("duplicateSku", 10000, 100, 10,
                                List.of(new VariantOptionValueRequest(existType.getId(), existValue.getId()),
                                        new VariantOptionValueRequest(newOptionType.getId(), newOptionValue1.getId()))),
                        new ProductVariantRequest("duplicateSku", 10000, 100, 10,
                                List.of(new VariantOptionValueRequest(existType.getId(),existValue.getId()),
                                        new VariantOptionValueRequest(newOptionType.getId(), newOptionValue2.getId()))))
        );

        assertThatThrownBy(() -> productService.saveProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_VARIANT_SKU_CONFLICT));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(상품 옵션 타입과 다른 상품 변형 옵션 타입이 있는 경우)")
    void saveProductTest_integration_optionValue_cardinality_violation(){
        ProductRequest request = createProductRequest();
        request.setProductOptionTypes(
                List.of(new ProductOptionTypeRequest(existType.getId(), 0))
        );
        request.setProductVariants(
                List.of(new ProductVariantRequest("sku", 10000, 100, 10,
                                List.of(new VariantOptionValueRequest(existType.getId(), existValue.getId()),
                                        new VariantOptionValueRequest(newOptionType.getId(), newOptionValue1.getId())))
        ));

        assertThatThrownBy(() -> productService.saveProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION));

        request.setProductVariants(
                List.of(new ProductVariantRequest("sku", 10000, 100, 10,
                        List.of(new VariantOptionValueRequest(newOptionType.getId(), newOptionValue1.getId())))
                ));

        assertThatThrownBy(() -> productService.saveProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION));

        request.setProductOptionTypes(
                List.of(new ProductOptionTypeRequest(existType.getId(), 0),
                        new ProductOptionTypeRequest(newOptionType.getId(), 1))
        );
        request.setProductVariants(
                List.of(new ProductVariantRequest("sku", 10000, 100, 10,
                        List.of(new VariantOptionValueRequest(existType.getId(), newOptionValue1.getId())))
                ));

        assertThatThrownBy(() -> productService.saveProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(상품 변형의 옵션 값이 중복될 경우)")
    @Transactional
    void saveProduct_integration_duplicate_variant_options(){
        ProductRequest request = createProductRequest();

        request.setProductVariants(
                List.of(
                        new ProductVariantRequest("sku1", 100, 100, 10,
                                List.of(
                                        new VariantOptionValueRequest(existType.getId(), existValue.getId())
                                )),
                        new ProductVariantRequest("sku2", 100, 100, 10,
                                List.of(
                                        new VariantOptionValueRequest(existType.getId(),existValue.getId())
                                ))
                )
        );

        assertThatThrownBy(() -> productService.saveProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_VARIANT_OPTION_VALUE_CONFLICT));

    }

    @Test
    @DisplayName("상품 저장 테스트-실패(DB에 동일한 SKU가 존재할 경우)")
    @Transactional
    void saveProduct_integration_conflict_sku(){
        Products product = new Products("existProductName", "description", category);
        ProductVariants productVariants = new ProductVariants("duplicateSku", 100, 10, 10);
        product.addVariants(List.of(productVariants));
        productsRepository.save(product);
        em.flush(); em.clear();

        ProductRequest request = createProductRequest();
        request.setProductOptionTypes(List.of());
        request.setProductVariants(List.of(new ProductVariantRequest("duplicateSku", 100, 10, 10, List.of())));

        assertThatThrownBy(() -> productService.saveProduct(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage(getMessage(PRODUCT_VARIANT_SKU_CONFLICT));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(카테고리 없음)")
    void saveProduct_integration_notFound_category(){
        ProductRequest request = createProductRequest();
        request.setCategoryId(999L);

        assertThatThrownBy(() -> productService.saveProduct(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(CATEGORY_NOT_FOUND));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(최하위 카테고리가 아님)")
    @Transactional
    void saveProduct_integration_notLeaf_category(){
        Categories leaf = new Categories("leaf", null);
        category.addChild(leaf);
        em.flush(); em.clear();
        ProductRequest request = createProductRequest();
        assertThatThrownBy(() -> productService.saveProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_CATEGORY_BAD_REQUEST));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(옵션 타입 없음)")
    void saveProduct_integration_notFound_optionType(){
        ProductRequest request = createProductRequest();
        request.setProductOptionTypes(
                List.of(new ProductOptionTypeRequest(999L, 0))
        );

        request.setProductVariants(
                List.of(new ProductVariantRequest(
                        "sku", 100, 10, 10,
                        List.of(new VariantOptionValueRequest(999L, 10L))
                ))
        );

        assertThatThrownBy(() -> productService.saveProduct(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(OPTION_TYPE_NOT_FOUND));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(옵션 값이 옵션 타입의 연관 엔티티가 아닌경우)")
    void saveProduct_integration_optionValue_notMatch_type(){
        ProductRequest request = createProductRequest();
        request.setProductVariants(
                List.of(new ProductVariantRequest("sku", 100, 10, 10,
                        List.of(new VariantOptionValueRequest(existType.getId(), 100L))))
        );

        assertThatThrownBy(() -> productService.saveProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_OPTION_VALUE_NOT_MATCH_TYPE));
    }

    @Test
    @DisplayName("상품 목록 조회 테스트-성공")
    @Transactional
    void getProductsTest_integration_success(){
        ProductImages productImage = createProductImages("http://test.jpg", 0);
        ProductOptionTypes productOptionType = createProductOptionType(existType);
        ProductVariants productVariant = createProductVariants("sku", 10000, 100, 10, existValue);

        Products product = createProduct("productName", "description", category,
                List.of(productImage), List.of(productOptionType), List.of(productVariant));

        Products savedProduct = productsRepository.save(product);
        em.flush(); em.clear();

        ProductSearch productSearch = new ProductSearch(category.getId(), "", null);
        Pageable pageable = PageRequest.of(0, 10);
        PageDto<ProductSummaryResponse> response = productService.getProducts(productSearch, pageable);

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
        ProductOptionTypes productOptionType = createProductOptionType(existType);
        ProductVariants productVariant = createProductVariants("sku", 10000, 100, 10, existValue);

        Products product = createProduct("productName", "description", category,
                List.of(productImage), List.of(productOptionType), List.of(productVariant));

        Products savedProduct = productsRepository.save(product);
        em.flush(); em.clear();

        ProductResponse response = productService.getProductById(savedProduct.getId());

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
                        tuple(existType.getId(), existType.getName())
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
                        tuple(existValue.getId(), existType.getId(), existValue.getOptionValue())
                );
    }

    @Test
    @DisplayName("상품 상세 조회 테스트-실패(상품을 찾을 수 없음)")
    void getProductByIdTest_integration_notFound(){
        assertThatThrownBy(() -> productService.getProductById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(PRODUCT_NOT_FOUND));
    }

    @Test
    @DisplayName("인기 상품 조회 테스트-성공")
    @Transactional
    void getPopularProductsTest_integration_success(){
        ProductImages product1Image = createProductImages("http://test.jpg", 0);
        ProductOptionTypes product1OptionType = createProductOptionType(existType);
        ProductVariants product1Variant = createProductVariants("sku1", 10000, 100, 10, existValue);
        for(long i=1; i<6; i++){
            product1Variant.addReview(i, "user"+i, 4, "good", List.of());
        }

        ProductImages product2Image = createProductImages("http://test.jpg", 0);
        ProductOptionTypes product2OptionType = createProductOptionType(existType);
        ProductVariants product2Variant = createProductVariants("sku2", 10000, 100, 10, existValue);

        Products product1 = createProduct("productName", "description", category,
                List.of(product1Image), List.of(product1OptionType), List.of(product1Variant));
        Products product2 = createProduct("productName", "description", category,
                List.of(product2Image), List.of(product2OptionType), List.of(product2Variant));

        productsRepository.saveAll(List.of(product1, product2));
        em.flush(); em.clear();

        PageDto<ProductSummaryResponse> response = productService.getPopularProducts(0, 10, null);

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
        ProductOptionTypes productOptionType = createProductOptionType(existType);
        ProductVariants product1Variant = createProductVariants("sku1", 10000, 100, 10, existValue);
        for(long i=1; i<6; i++){
            product1Variant.addReview(i, "user"+i, 4, "good", List.of());
        }

        Products product = createProduct("productName", "description", category,
                List.of(productImage), List.of(productOptionType), List.of(product1Variant));

        productsRepository.save(product);
        em.flush(); em.clear();

        Pageable pageable = PageRequest.of(0, 10, Sort.Direction.DESC, "createAt");

        PageDto<ReviewResponse> response = productService.getReviewsByProductId(product.getId(), pageable);

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
                        tuple(existValue.getId(), existType.getId(), existValue.getOptionValue()),
                        tuple(existValue.getId(), existType.getId(), existValue.getOptionValue()),
                        tuple(existValue.getId(), existType.getId(), existValue.getOptionValue()),
                        tuple(existValue.getId(), existType.getId(), existValue.getOptionValue()),
                        tuple(existValue.getId(), existType.getId(), existValue.getOptionValue())
                );
    }

    @Test
    @DisplayName("상품 리뷰 목록 조회 테스트-실패(상품을 찾을 수 없음)")
    void getReviewsByProductIdTest_integration_notFound(){
        assertThatThrownBy(() -> productService.getReviewsByProductId(999L, PageRequest.of(0, 10)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(PRODUCT_NOT_FOUND));
    }

    @Test
    @DisplayName("상품 기본 정보 변경 테스트-성공")
    @Transactional
    void updateBasicInfoByIdTest_integration_success(){
        ProductImages productImage = createProductImages("http://test.jpg", 0);
        ProductOptionTypes productOptionType = createProductOptionType(existType);
        ProductVariants product1Variant = createProductVariants("sku1", 10000, 100, 10, existValue);
        for(long i=1; i<6; i++){
            product1Variant.addReview(i, "user"+i, 4, "good", List.of());
        }

        Products product = createProduct("productName", "description", category,
                List.of(productImage), List.of(productOptionType), List.of(product1Variant));

        productsRepository.save(product);
        em.flush(); em.clear();

        UpdateProductBasicRequest request = new UpdateProductBasicRequest("updatedName", null, null);

        ProductUpdateResponse response = productService.updateBasicInfoById(product.getId(), request);

        assertThat(response.getName()).isEqualTo("updatedName");
        assertThat(response.getDescription()).isEqualTo("description");
        assertThat(response.getCategoryId()).isEqualTo(category.getId());
    }

    @Test
    @DisplayName("상품 기본 정보 수정 테스트-실패(상품을 찾을 수 없음)")
    @Transactional
    void updateBasicInfoByIdTest_integration_notFound_product(){
        UpdateProductBasicRequest request = new UpdateProductBasicRequest("updatedName", null, null);

        assertThatThrownBy(() -> productService.updateBasicInfoById(999L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(PRODUCT_NOT_FOUND));
    }

    @Test
    @DisplayName("상품 기본 정보 수정 테스트-실패(상품을 찾을 수 없음)")
    @Transactional
    void updateBasicInfoByIdTest_integration_notFound_category(){
        ProductImages productImage = createProductImages("http://test.jpg", 0);
        ProductOptionTypes productOptionType = createProductOptionType(existType);
        ProductVariants product1Variant = createProductVariants("sku1", 10000, 100, 10, existValue);
        for(long i=1; i<6; i++){
            product1Variant.addReview(i, "user"+i, 4, "good", List.of());
        }

        Products product = createProduct("productName", "description", category,
                List.of(productImage), List.of(productOptionType), List.of(product1Variant));

        productsRepository.save(product);
        em.flush(); em.clear();

        UpdateProductBasicRequest request = new UpdateProductBasicRequest("updatedName", null, 999L);

        assertThatThrownBy(() -> productService.updateBasicInfoById(product.getId(), request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(CATEGORY_NOT_FOUND));
    }

    @Test
    @DisplayName("상품 삭제 테스트-성공")
    @Transactional
    void deleteProductByIdTest_integration_success(){
        ProductImages productImage = createProductImages("http://test.jpg", 0);
        ProductOptionTypes productOptionType = createProductOptionType(existType);
        ProductVariants product1Variant = createProductVariants("sku1", 10000, 100, 10, existValue);
        for(long i=1; i<6; i++){
            product1Variant.addReview(i, "user"+i, 4, "good", List.of());
        }

        Products product = createProduct("productName", "description", category,
                List.of(productImage), List.of(productOptionType), List.of(product1Variant));

        productsRepository.save(product);
        productService.deleteProductById(product.getId());
        em.flush(); em.clear();

        Optional<Products> result = productsRepository.findById(product.getId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("상품 삭제 테스트-실패(상품을 찾을 수 없음)")
    void deleteProductByIdTest_integration_notFoundProduct(){
        assertThatThrownBy(()-> productService.deleteProductById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(PRODUCT_NOT_FOUND));
    }



    private ProductRequest createProductRequest() {
        return new ProductRequest("productName", "product description", category.getId(),
                List.of(new ImageRequest("http://test.jpg")),
                List.of(new ProductOptionTypeRequest(existType.getId(), 0)),
                List.of(new ProductVariantRequest("sku", 100, 10, 10,
                        List.of(new VariantOptionValueRequest(existType.getId(), existValue.getId()))))
        );
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
}