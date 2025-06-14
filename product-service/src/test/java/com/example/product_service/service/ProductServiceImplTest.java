package com.example.product_service.service;

import com.example.product_service.dto.request.ProductImageRequestDto;
import com.example.product_service.dto.request.product.CreateVariantsRequestDto;
import com.example.product_service.dto.request.product.ProductBasicRequestDto;
import com.example.product_service.dto.request.product.ProductRequestDto;
import com.example.product_service.dto.request.product.VariantsRequestDto;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.product.ProductImageDto;
import com.example.product_service.dto.response.product.ProductResponseDto;
import com.example.product_service.dto.response.product.ProductSummaryDto;
import com.example.product_service.dto.response.product.VariantsResponseDto;
import com.example.product_service.entity.*;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.CategoriesRepository;
import com.example.product_service.repository.OptionTypesRepository;
import com.example.product_service.repository.ProductsRepository;
import com.example.product_service.repository.ReviewsRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@SpringBootTest
@Slf4j
class ProductServiceImplTest {

    @Autowired
    ProductService productService;
    @Autowired
    ProductsRepository productsRepository;
    @Autowired
    CategoriesRepository categoriesRepository;
    @Autowired
    OptionTypesRepository optionTypesRepository;
    @Autowired
    ReviewsRepository reviewsRepository;
    @MockitoSpyBean
    private CategoryService categoryService;

    Categories clothes;
    Categories T_shirt;
    Categories sweatShirt;

    OptionTypes size;
    OptionTypes color;

    OptionValues xl;
    OptionValues blue;
    OptionValues l;
    OptionValues red;
    
    @BeforeEach
    void initData(){
        clothes = new Categories("의류", null);
        T_shirt = new Categories("티셔츠", null);
        sweatShirt = new Categories("맨투맨", null);
        clothes.addChild(T_shirt);
        clothes.addChild(sweatShirt);

        size = new OptionTypes("사이즈");
        color = new OptionTypes("색상");

        xl = new OptionValues("XL", size);
        l = new OptionValues("l", size);

        blue = new OptionValues("BLUE", color);
        red = new OptionValues("RED", color);
        size.addOptionValue(xl);
        size.addOptionValue(l);
        color.addOptionValue(blue);
        color.addOptionValue(red);

        categoriesRepository.save(clothes);
        optionTypesRepository.saveAll(List.of(size, color));
    }

    @AfterEach
    void clearData(){
        productsRepository.deleteAll();
        categoriesRepository.deleteAll();
        optionTypesRepository.deleteAll();
        reviewsRepository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("상품 저장 테스트_성공")
    @Transactional
    void saveProductTest_success(){
        //초기 데이터
        //상품 Variants
        VariantsRequestDto variants = buildVariant(xl.getId(), blue.getId());
        ProductRequestDto requestDto =
                buildProductRequest(T_shirt.getId(), List.of(size.getId(), color.getId()), List.of(variants));
        //when
        ProductResponseDto responseDto = productService.saveProduct(requestDto);
        /* 상품 기본 정보 검증
        * 상품 id,
        * 상품 Name,
        * 상품 Description,
        * 상품 CategoryId
        * */
        assertThat(responseDto.getId()).isNotNull();
        assertThat(responseDto.getName()).isEqualTo(requestDto.getName());
        assertThat(responseDto.getDescription()).isEqualTo(requestDto.getDescription());
        assertThat(responseDto.getCategoryId()).isEqualTo(requestDto.getCategoryId());
        assertThat(responseDto.getRatingAvg()).isEqualTo(0.0);
        assertThat(responseDto.getTotalReviewCount()).isEqualTo(0);

        /* 상품 이미지 검증
        * 이미지 url , sortOrder
        * */
        assertThat(responseDto.getImages())
                .extracting(ProductImageDto::getImageUrl, ProductImageDto::getSortOrder)
                .containsExactly(
                        tuple("http://test1.jpg", 0),
                        tuple("http://test2.jpg", 1)
                );

        /* 상품 Variants 검증
        * sku,
        * price,
        * stockQuantity,
        * discountValue
        * */
        assertThat(responseDto.getVariants()).hasSize(1);
        assertThat(responseDto.getVariants())
                .extracting(
                        VariantsResponseDto::getPrice,
                        VariantsResponseDto::getStockQuantity,
                        VariantsResponseDto::getDiscountValue
                )
                .containsExactlyInAnyOrder(
                        tuple( 29000, 30, 10)
                );

        /* DB 검증
        * imageSize
        * optionTypeSize,
        * variantsSize
        * */

        Products product = productsRepository.findById(responseDto.getId())
                .orElseThrow();
        assertThat(product.getImages()).hasSize(requestDto.getImageUrls().size());
        assertThat(product.getProductOptionTypes()).hasSize(requestDto.getOptionTypeIds().size());
        assertThat(product.getProductVariants()).hasSize(requestDto.getVariants().size());
    }

    @TestFactory
    @DisplayName("상품 저장_예외 테스트")
    Stream<DynamicTest> invalidCategoryTests(){
        // 정상 Variants
        VariantsRequestDto variant = buildVariant(xl.getId(), blue.getId());
        // 없는 OptionValue Variants
        VariantsRequestDto invalidOptionValueVariant = buildVariant(xl.getId(), 999L);
        return Stream.of(
                dynamicTest("존재하지 않는 카테고리", () -> {
                    ProductRequestDto requestDto = buildProductRequest(
                            999L, // 존재하지 않는 카테고리
                            List.of(size.getId(), color.getId()),
                            List.of(variant)
                    );
                    assertThatThrownBy(() -> productService.saveProduct(requestDto))
                            .isInstanceOf(NotFoundException.class)
                            .hasMessage("Not Found Category");
                }),

                dynamicTest("최하위 카테고리 아님", () -> {
                    ProductRequestDto requestDto = buildProductRequest(
                            clothes.getId(), // 상위 카테고리
                            List.of(size.getId(), color.getId()),
                            List.of(variant)
                    );
                    assertThatThrownBy(() -> productService.saveProduct(requestDto))
                            .isInstanceOf(BadRequestException.class)
                            .hasMessage("Category must be lowest level");
                }),

                dynamicTest("존재하지 않는 옵션 타입", () -> {
                    ProductRequestDto requestDto = buildProductRequest(
                            T_shirt.getId(),
                            List.of(size.getId(), 999L), //존재하는 옵션 타입 포함
                            List.of(variant)
                    );

                    assertThatThrownBy(() -> productService.saveProduct(requestDto))
                            .isInstanceOf(NotFoundException.class)
                            .hasMessage("Invalid OptionType Ids : [999]");
                }),

                dynamicTest("존재하지 않는 옵션 값", () -> {
                    ProductRequestDto requestDto = buildProductRequest(
                            T_shirt.getId(),
                            List.of(size.getId(), color.getId()),
                            List.of(invalidOptionValueVariant)
                    );

                    assertThatThrownBy(() -> productService.saveProduct(requestDto))
                            .isInstanceOf(NotFoundException.class)
                            .hasMessage("Invalid OptionValue Ids : [999]");
                })
        );
    }

    @Test
    @DisplayName("상품 목록 조회 테스트")
    @Transactional
    void getProductListTest(){
        Products over = new Products("오버핏 반팔티", "오버핏 반팔티 아이콘 NSW 퓨추라 스우시 반팔 티셔츠", T_shirt);
        over.addProductOptionTypes(size, 0 , true);
        over.addProductOptionTypes(color, 1, true);
        over.addProductVariants("TS-XL-BLUE",10000, 10, 10, List.of(xl,blue));
        over.addProductVariants("TS-L-RED", 10000, 10, 20, List.of(l,red));
        over.addImage("http://over_thumbnail.jpg", 0);
        ProductVariants variants = over.getProductVariants().stream().filter(variant -> Objects.equals(variant.getSku(), "TS-XL-BLUE")).findFirst().get();
        variants.addReview(1L, "user1", 4, "리뷰테스트",List.of());

        Products nike = new Products("나이키 티셔츠", "나이키 티셔츠 빅로고 나이키 기능성 반팔 티셔츠", T_shirt);
        nike.addProductOptionTypes(size , 0 , true);
        nike.addProductOptionTypes(color, 1, true);
        nike.addProductVariants("NIKE-XL-BLUE", 20000, 10, 10, List.of(xl,blue));
        nike.addProductVariants("NIKE-L-RED", 20000, 10, 20, List.of(l,red));
        nike.addImage("http://nike_thumbnail.jpg",0);
        ProductVariants variants1 = nike.getProductVariants().stream().filter(variant -> variant.getSku() == "NIKE-XL-BLUE").findFirst().get();
        variants1.addReview(1L, "user1", 5, "리뷰테스트", List.of());
        ProductVariants variants2 = nike.getProductVariants().stream().filter(variant -> variant.getSku() == "NIKE-L-RED").findFirst().get();
        variants2.addReview(1L, "user2", 4, "리뷰테스트", List.of());
        productsRepository.saveAll(List.of(over,nike));

        doReturn(List.of()).when(categoryService).getCategoryAndDescendantIds(nullable(Long.class));
        PageDto<ProductSummaryDto> result = productService.getProductList(PageRequest.of(0, 10, Sort.Direction.ASC, "id"), null, null, null);
        assertThat(result.getContent().size()).isEqualTo(2);
        assertThat(result.getCurrentPage()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(10);
        assertThat(result.getTotalElement()).isEqualTo(2);

        assertThat(result.getContent())
                .extracting(
                        ProductSummaryDto::getId,
                        ProductSummaryDto::getName,
                        ProductSummaryDto::getDescription,
                        ProductSummaryDto::getThumbnailUrl,
                        ProductSummaryDto::getCategoryName,
                        ProductSummaryDto::getRatingAvg,
                        ProductSummaryDto::getTotalReviewCount,
                        ProductSummaryDto::getOriginPrice,
                        ProductSummaryDto::getDiscountPrice,
                        ProductSummaryDto::getDiscountValue
                )
                .containsExactlyInAnyOrder(
                    tuple(over.getId(), over.getName(), over.getDescription(), "http://over_thumbnail.jpg",
                            T_shirt.getName(), 4.0, 1, 10000, 8000, 20),
                    tuple(nike.getId(), nike.getName(), nike.getDescription(), "http://nike_thumbnail.jpg",
                            T_shirt.getName(), 4.5, 2, 20000, 16000, 20)
                );
    }

    @Test
    @DisplayName("상품 상세 조회")
    @Transactional
    void getProductDetailsTest(){
        Products over = new Products("오버핏 반팔티", "오버핏 반팔티 아이콘 NSW 퓨추라 스우시 반팔 티셔츠", T_shirt);
        over.addProductOptionTypes(size, 0 , true);
        over.addProductOptionTypes(color, 1, true);
        over.addProductVariants("TS-XL-BLUE",10000, 10, 10, List.of(xl,blue));
        over.addProductVariants("TS-L-RED", 10000, 10, 20, List.of(l,red));
        over.addImage("http://over_thumbnail.jpg", 0);
        ProductVariants variants = over.getProductVariants().stream().filter(variant -> variant.getSku() == "TS-XL-BLUE").findFirst().get();
        variants.addReview(1L, "user1", 4, "테스트리뷰", List.of());
        variants.addReview(2L, "user2", 5, "테스트리뷰", List.of());
        over = productsRepository.save(over);

        ProductResponseDto response = productService.getProductDetails(over.getId());


        //상품 기본 정보 검증
        assertThat(response.getId()).isEqualTo(over.getId());
        assertThat(response.getName()).isEqualTo(over.getName());
        assertThat(response.getDescription()).isEqualTo(over.getDescription());
        assertThat(response.getRatingAvg()).isEqualTo(4.5);
        assertThat(response.getTotalReviewCount()).isEqualTo(2);

        //카테고리 검증
        assertThat(response.getCategoryId()).isEqualTo(T_shirt.getId());

        //이미지 검증
        assertThat(response.getImages().size()).isEqualTo(1);
        assertThat(response.getImages())
                .extracting(
                        ProductImageDto::getImageUrl,
                        ProductImageDto::getSortOrder
                )
                .containsExactlyInAnyOrder(
                        tuple("http://over_thumbnail.jpg", 0)
                );

        //options 검증
        assertThat(response.getOptions().size()).isEqualTo(2);

        //Variants 검증
        assertThat(response.getVariants().size()).isEqualTo(2);
        assertThat(response.getVariants())
                .extracting(
                        VariantsResponseDto::getSku,
                        VariantsResponseDto::getPrice,
                        VariantsResponseDto::getStockQuantity,
                        VariantsResponseDto::getDiscountValue,
                        VariantsResponseDto::getOptionValueId
                )
                .containsExactlyInAnyOrder(
                        tuple("TS-XL-BLUE",10000, 10, 10, List.of(xl.getId(), blue.getId())),
                        tuple("TS-L-RED",10000, 10, 20, List.of(l.getId(), red.getId()))
                );
    }

    @Test
    @DisplayName("상품 기본정보 변경")
    @Transactional
    void modifyProductBasicTest(){
        Products save = productsRepository.save(new Products("상품1", "상품 설명1", T_shirt));
        ProductBasicRequestDto requestDto = new ProductBasicRequestDto("변경상품1", "변경 상품 설명", sweatShirt.getId());

        ProductResponseDto responseDto = productService.modifyProductBasic(save.getId(), requestDto);

        assertThat(responseDto.getName()).isEqualTo(requestDto.getName());
        assertThat(responseDto.getDescription()).isEqualTo(requestDto.getDescription());
        assertThat(responseDto.getCategoryId()).isEqualTo(requestDto.getCategoryId());

        Products products = productsRepository.findById(save.getId()).orElseThrow();

        assertThat(products.getName()).isEqualTo(requestDto.getName());
        assertThat(products.getDescription()).isEqualTo(requestDto.getDescription());
        assertThat(products.getCategory()).isEqualTo(sweatShirt);
    }

    @Test
    @DisplayName("인기 상품 조회")
    @Transactional
    void getPopularProductListTest(){
        Products save1 = new Products("상품1", "상품 설명1", T_shirt);
        save1.addProductVariants("TS-XL-BLUE", 3000, 30, 10, List.of(xl, blue));
        save1 = productsRepository.save(save1);
        ProductVariants save1Variant1 = save1.getProductVariants().stream()
                .filter(var -> Objects.equals(var.getSku(), "TS-XL-BLUE")).findFirst().orElseThrow();

        for (long i = 0; i < 5; i++) {
            save1Variant1.addReview(i, "테스터" + i, 4, "리뷰", List.of());
        }

        Products save2 = new Products("상품2", "상품 설명 2", sweatShirt);
        save2.addProductVariants("SS-XL-BLUE", 3000, 30, 10, List.of(xl, blue));
        save2 = productsRepository.save(save2);
        ProductVariants save2Variant1 = save2.getProductVariants().stream().filter(var -> Objects.equals(var.getSku(), "SS-XL-BLUE"))
                .findFirst().orElseThrow();

        for(long i = 0; i < 3; i++){
            save2Variant1.addReview(i, "테스터" + i, 2, "리뷰" , List.of());
        }

        Products save3 = new Products("상품3", "상품 설명 3", sweatShirt);
        save3.addProductVariants("SS-L-BLUE", 3000, 30, 10, List.of(l, blue));

        productsRepository.saveAll(List.of(save1, save2, save3));
        doReturn(null).when(categoryService).getCategoryAndDescendantIds(nullable(Long.class));
        PageDto<ProductSummaryDto> result = productService.getPopularProductList(PageRequest.of(0, 10), null);

        assertThat(result.getContent().size()).isEqualTo(2);
        assertThat(result.getTotalElement()).isEqualTo(2);
        assertThat(result.getTotalPage()).isEqualTo(1);
        assertThat(result.getContent())
                .extracting(
                        ProductSummaryDto::getName,
                        ProductSummaryDto::getDescription,
                        ProductSummaryDto::getCategoryName,
                        ProductSummaryDto::getRatingAvg,
                        ProductSummaryDto::getTotalReviewCount,
                        ProductSummaryDto::getOriginPrice,
                        ProductSummaryDto::getDiscountPrice,
                        ProductSummaryDto::getDiscountValue)
                .containsExactly(
                        tuple("상품1", "상품 설명1", T_shirt.getName(), 4.0, 5, 3000, 2700, 10),
                        tuple("상품2", "상품 설명 2", sweatShirt.getName(), 2.0, 3, 3000, 2700, 10)
                );
    }

    private VariantsRequestDto buildVariant(Long... optionValueIds){
        return new VariantsRequestDto(

                29000,
                30,
                10,
                List.of(optionValueIds)
        );
    }

    private ProductRequestDto buildProductRequest(Long categoryId, List<Long> optionTypeIds, List<VariantsRequestDto> variants){
        return new ProductRequestDto(
                "나이키 티셔츠",
                "나이키 반팔티 아이콘 NSW 퓨추라 스우시 반팔 티셔츠",
                categoryId,
                List.of("http://test1.jpg", "http://test2.jpg"),
                optionTypeIds,
                variants
        );
    }
}