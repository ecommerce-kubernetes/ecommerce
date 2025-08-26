package com.example.product_service.service;

import com.example.product_service.dto.request.image.AddImageRequest;
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
public class ProductApplicationServiceTest {
    @Autowired
    ProductsRepository productsRepository;
    @Autowired
    OptionTypeRepository optionTypeRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    ProductApplicationService productApplicationService;

    @Autowired
    EntityManager em;

    OptionTypes storage;
    OptionTypes color;
    OptionValues red;
    OptionValues blue;
    OptionValues gb_128;
    OptionValues gb_256;
    Categories electronic;
    @BeforeEach
    void saveFixture(){
        storage = new OptionTypes("용량");
        color = new OptionTypes("색상");
        gb_128 = new OptionValues("128GB");
        gb_256 = new OptionValues("256GB");
        red = new OptionValues("RED");
        blue = new OptionValues("BLUE");
        electronic = new Categories("전자 기기", "http://electronic.jpg");
        storage.addOptionValue(gb_128);
        storage.addOptionValue(gb_256);
        color.addOptionValue(red);
        color.addOptionValue(blue);
        optionTypeRepository.saveAll(List.of(storage, color));
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
    @DisplayName("상품 저장 테스트-성공")
    @Transactional
    void saveProduct_integration_success(){
        ProductRequest request = createProductRequest();

        ProductResponse response = productApplicationService.saveProduct(request);

        assertThat(response)
                .extracting("name", "description", "categoryId")
                .containsExactly("IPhone 16", "IPhone 모델 16", electronic.getId());

        assertThat(response.getImages())
                .extracting("url", "sortOrder")
                .containsExactlyInAnyOrder(
                        tuple("http://iphone16.jpg", 0)
                );

        assertThat(response.getProductOptionTypes())
                .extracting("id", "name")
                .containsExactlyInAnyOrder(
                        tuple(storage.getId(), storage.getName())
                );

        assertThat(response.getProductVariants())
                .extracting("sku", "price", "stockQuantity", "discountRate")
                .containsExactlyInAnyOrder(
                        tuple("IPHONE16-128GB", 100, 10, 10)
                );

        ProductVariantResponse variant = response.getProductVariants().stream()
                .filter(v -> "IPHONE16-128GB".equals(v.getSku()))
                .findFirst()
                .orElseThrow();

        assertThat(variant.getOptionValues()).hasSize(1);
        assertThat(variant.getOptionValues())
                .extracting("valueId", "typeId", "valueName")
                .containsExactly(tuple(gb_128.getId(), storage.getId(), gb_128.getOptionValue()));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(요청 Body에 중복된 productOptionTypeId)")
    void saveProductTest_integration_option_type_typeId_badRequest(){
        ProductRequest request = createProductRequest();
        request.setProductOptionTypes(List.of(new ProductOptionTypeRequest(storage.getId(), 0),
                new ProductOptionTypeRequest(storage.getId(), 1)));

        assertThatThrownBy(() -> productApplicationService.saveProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_OPTION_TYPE_TYPE_BAD_REQUEST));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(요청 Body에 중복된 priority)")
    void saveProductTest_integration_option_type_priority_badRequest(){
        ProductRequest request = createProductRequest();
        request.setProductOptionTypes(List.of(new ProductOptionTypeRequest(storage.getId(), 0),
                new ProductOptionTypeRequest(color.getId(), 0)));

        assertThatThrownBy(() -> productApplicationService.saveProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_OPTION_TYPE_PRIORITY_BAD_REQUEST));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(요청 Body에 중복된 SKU)")
    void saveProductTest_integration_request_sku_duplicate(){
        ProductRequest request = createProductRequest();
        request.setProductOptionTypes(
                List.of(new ProductOptionTypeRequest(storage.getId(), 0),
                        new ProductOptionTypeRequest(color.getId(), 1))
        );
        request.setProductVariants(
                List.of(new ProductVariantRequest("duplicateSku", 10000, 100, 10,
                                List.of(new VariantOptionValueRequest(storage.getId(), gb_128.getId()),
                                        new VariantOptionValueRequest(color.getId(), red.getId()))),
                        new ProductVariantRequest("duplicateSku", 10000, 100, 10,
                                List.of(new VariantOptionValueRequest(storage.getId(), gb_128.getId()),
                                        new VariantOptionValueRequest(color.getId(), blue.getId()))))
        );

        assertThatThrownBy(() -> productApplicationService.saveProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_VARIANT_SKU_CONFLICT));
    }
    @Test
    @DisplayName("상품 저장 테스트-실패(상품 옵션 타입과 다른 상품 변형 옵션 타입이 있는 경우)")
    void saveProductTest_integration_optionValue_cardinality_violation(){
        ProductRequest request = createProductRequest();
        request.setProductOptionTypes(
                List.of(new ProductOptionTypeRequest(storage.getId(), 0))
        );

        // 상품 옵션 타입 < 상품 변형 옵션
        request.setProductVariants(
                List.of(new ProductVariantRequest("IPHONE16-128GB-RED", 10000, 100, 10,
                        List.of(new VariantOptionValueRequest(storage.getId(), gb_128.getId()),
                                new VariantOptionValueRequest(color.getId(), red.getId())))
                ));

        assertThatThrownBy(() -> productApplicationService.saveProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION));

        // 상품 옵션 타입 != 상품 변형 옵션
        request.setProductVariants(
                List.of(new ProductVariantRequest("IPHONE16-128GB-RED", 10000, 100, 10,
                        List.of(new VariantOptionValueRequest(color.getId(), red.getId())))
                ));

        assertThatThrownBy(() -> productApplicationService.saveProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION));

        // 상품 옵션 > 상품 변형 옵션
        request.setProductOptionTypes(
                List.of(new ProductOptionTypeRequest(storage.getId(), 0),
                        new ProductOptionTypeRequest(color.getId(), 1))
        );
        request.setProductVariants(
                List.of(new ProductVariantRequest("IPHONE16-", 10000, 100, 10,
                        List.of(new VariantOptionValueRequest(storage.getId(), gb_128.getId())))
                ));

        assertThatThrownBy(() -> productApplicationService.saveProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(상품 변형의 옵션 값이 중복될 경우)")
    @Transactional
    void saveProduct_integration_duplicate_variant_options(){
        ProductRequest request = createProductRequest();

        // 동일한 옵션을 추가
        request.setProductVariants(
                List.of(
                        new ProductVariantRequest("IPHONE16-128GB", 100, 100, 10,
                                List.of(
                                        new VariantOptionValueRequest(storage.getId(), gb_128.getId())
                                )),
                        new ProductVariantRequest("IPHONE16-256GB", 100, 100, 10,
                                List.of(
                                        new VariantOptionValueRequest(storage.getId(), gb_128.getId())
                                ))
                )
        );

        assertThatThrownBy(() -> productApplicationService.saveProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_VARIANT_OPTION_VALUE_CONFLICT));

    }

    @Test
    @DisplayName("상품 저장 테스트-실패(DB에 동일한 SKU가 존재할 경우)")
    @Transactional
    void saveProduct_integration_conflict_sku(){
        Products product = new Products("IPhone 16", "IPhone Model 16", electronic);
        ProductVariants productVariants = new ProductVariants("duplicateSku", 100, 10, 10);
        product.addVariants(List.of(productVariants));
        productsRepository.save(product);
        em.flush(); em.clear();

        ProductRequest request = createProductRequest();
        request.setProductOptionTypes(List.of());
        request.setProductVariants(List.of(new ProductVariantRequest("duplicateSku", 100, 10, 10, List.of())));

        assertThatThrownBy(() -> productApplicationService.saveProduct(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage(getMessage(PRODUCT_VARIANT_SKU_CONFLICT));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(카테고리 없음)")
    void saveProduct_integration_notFound_category(){
        ProductRequest request = createProductRequest();
        request.setCategoryId(999L);

        assertThatThrownBy(() -> productApplicationService.saveProduct(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(CATEGORY_NOT_FOUND));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(최하위 카테고리가 아님)")
    @Transactional
    void saveProduct_integration_notLeaf_category(){
        Categories leaf = new Categories("핸드폰", null);
        electronic.addChild(leaf);
        em.flush(); em.clear();
        ProductRequest request = createProductRequest();
        assertThatThrownBy(() -> productApplicationService.saveProduct(request))
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
                        "IPHONE16-XL", 100, 10, 10,
                        List.of(new VariantOptionValueRequest(999L, 10L))
                ))
        );

        assertThatThrownBy(() -> productApplicationService.saveProduct(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(OPTION_TYPE_NOT_FOUND));
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(옵션 값이 옵션 타입의 연관 엔티티가 아닌경우)")
    @Transactional
    void saveProduct_integration_optionValue_notMatch_type(){
        ProductRequest request = createProductRequest();
        request.setProductVariants(
                List.of(new ProductVariantRequest("IPHONE16-128GB", 100, 10, 10,
                        List.of(new VariantOptionValueRequest(storage.getId(), red.getId()))))
        );
        assertThatThrownBy(() -> productApplicationService.saveProduct(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_OPTION_VALUE_NOT_MATCH_TYPE));
    }
    @Test
    @DisplayName("상품 기본 정보 변경 테스트-성공")
    @Transactional
    void updateBasicInfoByIdTest_integration_success(){
        ProductImages productImage = createProductImages("http://iphone16-1.jpg", 0);
        ProductOptionTypes productOptionType = createProductOptionType(storage);
        ProductVariants product1Variant = createProductVariants("IPHONE16-128GB", 10000, 100, 10, gb_128);
        for(long i=1; i<6; i++){
            product1Variant.addReview(i, "user"+i, 4, "good", List.of());
        }

        Products product = createProduct("IPhone 16", "IPhone Model 16", electronic,
                List.of(productImage), List.of(productOptionType), List.of(product1Variant));

        productsRepository.save(product);
        em.flush(); em.clear();

        UpdateProductBasicRequest request = new UpdateProductBasicRequest("IPhone 16 Pro", "IPhone Model 16 Pro", null);

        ProductUpdateResponse response = productApplicationService.updateBasicInfoById(product.getId(), request);

        assertThat(response.getName()).isEqualTo("IPhone 16 Pro");
        assertThat(response.getDescription()).isEqualTo("IPhone Model 16 Pro");
        assertThat(response.getCategoryId()).isEqualTo(electronic.getId());
    }

    @Test
    @DisplayName("상품 기본 정보 수정 테스트-실패(상품을 찾을 수 없음)")
    @Transactional
    void updateBasicInfoByIdTest_integration_notFound_product(){
        UpdateProductBasicRequest request = new UpdateProductBasicRequest("IPhone 16", null, null);

        assertThatThrownBy(() -> productApplicationService.updateBasicInfoById(999L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(PRODUCT_NOT_FOUND));
    }

    @Test
    @DisplayName("상품 기본 정보 수정 테스트-실패(카테고리를 찾을 수 없음)")
    @Transactional
    void updateBasicInfoByIdTest_integration_notFound_category(){
        ProductImages productImage = createProductImages("http://iphone16.jpg", 0);
        ProductOptionTypes productOptionType = createProductOptionType(storage);
        ProductVariants product1Variant = createProductVariants("IPHONE16-128GB", 10000, 100, 10, gb_128);
        for(long i=1; i<6; i++){
            product1Variant.addReview(i, "user"+i, 4, "good", List.of());
        }

        Products product = createProduct("IPhone 16", "IPhone Model 16", electronic,
                List.of(productImage), List.of(productOptionType), List.of(product1Variant));

        productsRepository.save(product);
        em.flush(); em.clear();

        UpdateProductBasicRequest request = new UpdateProductBasicRequest("IPhone 17", null, 999L);

        assertThatThrownBy(() -> productApplicationService.updateBasicInfoById(product.getId(), request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(CATEGORY_NOT_FOUND));
    }

    @Test
    @DisplayName("상품 기본 정보 수정 테스트-실패(카테고리가 최하위 카테고리가 아님)")
    @Transactional
    void updateBasicInfoByIdTest_integration_badRequest_category(){
        ProductImages productImage = createProductImages("http://iphone16.jpg", 0);
        ProductOptionTypes productOptionType = createProductOptionType(storage);
        ProductVariants product1Variant = createProductVariants("IPHONE16-128GB", 10000, 100, 10, gb_128);
        for(long i=1; i<6; i++){
            product1Variant.addReview(i, "user"+i, 4, "good", List.of());
        }

        Products product = createProduct("IPhone 16", "IPhone Model 16", electronic,
                List.of(productImage), List.of(productOptionType), List.of(product1Variant));

        productsRepository.save(product);
        Categories root = new Categories("상위 카테고리", null);
        Categories child = new Categories("하위 카테고리", null);
        root.addChild(child);
        categoryRepository.save(root);
        em.flush(); em.clear();

        UpdateProductBasicRequest request = new UpdateProductBasicRequest("IPhone 17", null, root.getId());
        assertThatThrownBy(() -> productApplicationService.updateBasicInfoById(product.getId(), request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_CATEGORY_BAD_REQUEST));
    }

    @Test
    @DisplayName("상품 삭제 테스트-성공")
    @Transactional
    void deleteProductByIdTest_integration_success(){
        ProductImages productImage = createProductImages("http://iphone16.jpg", 0);
        ProductOptionTypes productOptionType = createProductOptionType(storage);
        ProductVariants product1Variant = createProductVariants("IPHONE16-128GB", 10000, 100, 10, gb_128);
        for(long i=1; i<6; i++){
            product1Variant.addReview(i, "user"+i, 4, "good", List.of());
        }

        Products product = createProduct("IPhone 16", "IPhone Model 16", electronic,
                List.of(productImage), List.of(productOptionType), List.of(product1Variant));

        productsRepository.save(product);
        productApplicationService.deleteProductById(product.getId());
        em.flush(); em.clear();

        Optional<Products> result = productsRepository.findById(product.getId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("상품 삭제 테스트-실패(상품을 찾을 수 없음)")
    void deleteProductByIdTest_integration_notFoundProduct(){
        assertThatThrownBy(()-> productApplicationService.deleteProductById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(PRODUCT_NOT_FOUND));
    }

    @Test
    @DisplayName("상품 이미지 추가 테스트-성공")
    @Transactional
    void addImagesTest_integration_success(){
        AddImageRequest request = new AddImageRequest(List.of("http://iphone16-2.jpg", "http://iphone16-3.jpg"));
        ProductImages productImage = createProductImages("http://iphone16-1.jpg", 0);
        ProductOptionTypes productOptionType = createProductOptionType(storage);
        ProductVariants product1Variant = createProductVariants("IPHONE16-128GB", 10000, 100, 10, gb_128);
        for(long i=1; i<6; i++){
            product1Variant.addReview(i, "user"+i, 4, "good", List.of());
        }

        Products product = createProduct("IPhone 16", "IPhone Model 16", electronic,
                List.of(productImage), List.of(productOptionType), List.of(product1Variant));

        productsRepository.save(product);
        em.flush(); em.clear();

        List<ImageResponse> response = productApplicationService.addImages(product.getId(), request);

        assertThat(response).hasSize(3);
        assertThat(response).extracting("url", "sortOrder")
                .containsExactlyInAnyOrder(
                        tuple("http://iphone16-1.jpg", 0),
                        tuple("http://iphone16-2.jpg", 1),
                        tuple("http://iphone16-3.jpg", 2)
                );
    }

    @Test
    @DisplayName("상품 이미지 추가 테스트-실패(상품을 찾을 수 없음)")
    void addImagesTest_integration_notFoundProduct(){
        AddImageRequest request = new AddImageRequest(List.of("http://iphone16-2.jpg", "http://iphone16-2.jpg"));
        assertThatThrownBy(() -> productApplicationService.addImages(999L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(PRODUCT_NOT_FOUND));
    }


    @Test
    @DisplayName("상품 변형 추가 테스트-성공")
    @Transactional
    void addVariantTest_integration_success(){
        ProductImages productImage = createProductImages("http://iphone16-1.jpg", 0);
        ProductOptionTypes productOptionType = createProductOptionType(storage);
        ProductVariants productVariant = createProductVariants("IPHONE16-128GB", 10000, 100, 10, gb_128);

        Products product = createProduct("IPhone 16", "IPhone Model 16", electronic,
                List.of(productImage), List.of(productOptionType), List.of(productVariant));

        productsRepository.save(product);

        em.flush(); em.clear();

        ProductVariantRequest request = new ProductVariantRequest("IPHONE16-256GB", 30000, 1000, 10,
                List.of(new VariantOptionValueRequest(storage.getId(), gb_256.getId())));

        ProductVariantResponse response = productApplicationService.addVariant(product.getId(), request);

        assertThat(response.getSku()).isEqualTo("IPHONE16-256GB");
        assertThat(response.getPrice()).isEqualTo(30000);
        assertThat(response.getStockQuantity()).isEqualTo(1000);
        assertThat(response.getDiscountRate()).isEqualTo(10);

        assertThat(response.getOptionValues()).extracting("valueId", "typeId", "valueName")
                .containsExactlyInAnyOrder(
                        tuple(gb_256.getId(), storage.getId(), gb_256.getOptionValue())
                );
    }

    @Test
    @DisplayName("상품 변형 추가 테스트-실패(상품을 찾을 수 없음)")
    @Transactional
    void addVariantTest_integration_notFound_product(){
        ProductVariantRequest request = new ProductVariantRequest("IPHONE16-256GB", 30000, 1000, 10,
                List.of(new VariantOptionValueRequest(storage.getId(), gb_256.getId())));

        assertThatThrownBy(() -> productApplicationService.addVariant(999L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(PRODUCT_NOT_FOUND));
    }

    @Test
    @DisplayName("상품 변형 추가 테스트-실패(요청 바디에 동일한 옵션 타입 아이디가 존재하는 경우)")
    @Transactional
    void addVariantTest_integration_variant_option_option_type_duplicate(){
        ProductImages productImage = createProductImages("http://iphone16-1.jpg", 0);
        ProductOptionTypes productOptionType = createProductOptionType(storage);
        ProductVariants productVariant = createProductVariants("IPHONE16-128GB", 10000, 100, 10, gb_128);

        Products product = createProduct("IPhone 16", "IPhone Model 16", electronic,
                List.of(productImage), List.of(productOptionType), List.of(productVariant));

        productsRepository.save(product);

        em.flush(); em.clear();

        ProductVariantRequest request = new ProductVariantRequest("IPHONE16-256GB", 30000, 1000, 10,
                List.of(new VariantOptionValueRequest(storage.getId(), gb_256.getId()),
                        new VariantOptionValueRequest(storage.getId(), gb_128.getId())));

        assertThatThrownBy(() -> productApplicationService.addVariant(product.getId(), request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION));
    }

    @Test
    @DisplayName("상품 변형 추가 테스트-실패(DB에 동일한 SKU 존재)")
    @Transactional
    void addVariantTest_integration_conflict_sku(){
        ProductImages productImage = createProductImages("http://iphone16-1.jpg", 0);
        ProductOptionTypes productOptionType = createProductOptionType(storage);
        ProductVariants productVariant = createProductVariants("DUPLICATE_SKU", 10000, 100, 10, gb_128);

        Products product = createProduct("IPhone 16", "IPhone Model 16", electronic,
                List.of(productImage), List.of(productOptionType), List.of(productVariant));

        productsRepository.save(product);

        em.flush(); em.clear();

        ProductVariantRequest request = new ProductVariantRequest("DUPLICATE_SKU", 30000, 1000, 10,
                List.of(new VariantOptionValueRequest(storage.getId(), gb_256.getId())));

        assertThatThrownBy(() -> productApplicationService.addVariant(product.getId(), request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage(getMessage(PRODUCT_VARIANT_SKU_CONFLICT));
    }

    @Test
    @DisplayName("상품 변형 추가 테스트-실패(옵션 값 찾을 수 없음)")
    @Transactional
    void addVariantTest_integration_notFound_optionValue(){
        ProductImages productImage = createProductImages("http://iphone16-1.jpg", 0);
        ProductOptionTypes productOptionType = createProductOptionType(storage);
        ProductVariants productVariant = createProductVariants("IPHONE16-128GB", 10000, 100, 10, gb_128);

        Products product = createProduct("IPhone 16", "IPhone Model 16", electronic,
                List.of(productImage), List.of(productOptionType), List.of(productVariant));

        productsRepository.save(product);
        em.flush(); em.clear();

        ProductVariantRequest request = new ProductVariantRequest("IPHONE16-256GB", 30000, 1000, 10,
                List.of(new VariantOptionValueRequest(storage.getId(), 999L)));
        assertThatThrownBy(() -> productApplicationService.addVariant(product.getId(), request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(OPTION_VALUE_NOT_FOUND));
    }

    @Test
    @DisplayName("상품 변형 추가 테스트-실패(상품 Variant의 OptionValue가 상품 OptionType의 하위 객체가 아닌 경우)")
    @Transactional
    void addVariantTest_integration_optionValue_notMatch_optionType(){
        ProductImages productImage = createProductImages("http://iphone16-1.jpg", 0);
        ProductOptionTypes productOptionType = createProductOptionType(storage);
        ProductVariants productVariant = createProductVariants("IPHONE16-128GB", 10000, 100, 10, gb_128);

        Products product = createProduct("IPhone 16", "IPhone Model 16", electronic,
                List.of(productImage), List.of(productOptionType), List.of(productVariant));

        productsRepository.save(product);
        em.flush(); em.clear();

        ProductVariantRequest request = new ProductVariantRequest("IPHONE16-256GB", 30000, 1000, 10,
                List.of(new VariantOptionValueRequest(storage.getId(), red.getId())));

        assertThatThrownBy(() -> productApplicationService.addVariant(product.getId(), request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_OPTION_VALUE_NOT_MATCH_TYPE));
    }

    @Test
    @DisplayName("상품 변형 추가 테스트-실패(ProductVariant Option 조합이 중복될 경우)")
    @Transactional
    void addVariantTest_unit_product_variant_option_combination_duplicate(){
        ProductImages productImage = createProductImages("http://iphone16-1.jpg", 0);
        ProductOptionTypes productOptionType = createProductOptionType(storage);
        ProductVariants productVariant = createProductVariants("IPHONE16-128GB", 10000, 100, 10, gb_128);

        Products product = createProduct("IPhone 16", "IPhone Model 16", electronic,
                List.of(productImage), List.of(productOptionType), List.of(productVariant));

        productsRepository.save(product);
        em.flush(); em.clear();

        ProductVariantRequest request = new ProductVariantRequest("NEW-IPHONE16-128GB", 30000, 1000, 10,
                List.of(new VariantOptionValueRequest(storage.getId(), gb_128.getId())));

        assertThatThrownBy(() -> productApplicationService.addVariant(product.getId(), request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_VARIANT_OPTION_VALUE_CONFLICT));
    }

    private ProductRequest createProductRequest() {
        return new ProductRequest("IPhone 16", "IPhone 모델 16", electronic.getId(),
                List.of("http://iphone16.jpg"),
                List.of(new ProductOptionTypeRequest(storage.getId(), 0)),
                List.of(new ProductVariantRequest("IPHONE16-128GB", 100, 10, 10,
                        List.of(new VariantOptionValueRequest(storage.getId(), gb_128.getId()))))
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

    private ProductVariants createProductVariants(String sku, int price, int stockQuantity, int discountValue, OptionValues optionValues){
        ProductVariantOptions productVariantOptions = new ProductVariantOptions(optionValues);

        ProductVariants productVariants = new ProductVariants(sku, price, stockQuantity, discountValue);
        productVariants.addProductVariantOption(productVariantOptions);
        return productVariants;
    }

    private ProductOptionTypes createProductOptionType(OptionTypes optionTypes){
        return new ProductOptionTypes(optionTypes, 0, true);
    }


    private ProductImages createProductImages(String imageUrl, int sortOrder){
        return new ProductImages(imageUrl, sortOrder);
    }
}
