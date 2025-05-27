package com.example.product_service.service;

import com.example.product_service.dto.request.product.ProductRequestDto;
import com.example.product_service.dto.request.product.VariantsRequestDto;
import com.example.product_service.dto.response.product.ProductImageDto;
import com.example.product_service.dto.response.product.ProductResponseDto;
import com.example.product_service.dto.response.product.VariantsResponseDto;
import com.example.product_service.entity.Categories;
import com.example.product_service.entity.OptionTypes;
import com.example.product_service.entity.OptionValues;
import com.example.product_service.entity.Products;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.repository.CategoriesRepository;
import com.example.product_service.repository.OptionTypesRepository;
import com.example.product_service.repository.ProductImagesRepository;
import com.example.product_service.repository.ProductsRepository;
import com.example.product_service.service.client.ImageClientService;
import com.example.product_service.service.kafka.KafkaProducer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.mockito.ArgumentMatchers.any;

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


    Categories clothes;
    Categories T_shirt;

    OptionTypes size;
    OptionTypes color;

    OptionValues xl;
    OptionValues blue;

    @BeforeEach
    void initData(){
        clothes = new Categories("의류", null);
        T_shirt = new Categories("티셔츠", null);
        clothes.addChild(T_shirt);

        size = new OptionTypes("사이즈");
        color = new OptionTypes("색상");

        xl = new OptionValues("XL", size);
        blue = new OptionValues("BLUE", color);

        size.addOptionValue(xl);
        color.addOptionValue(blue);

        categoriesRepository.save(clothes);
        optionTypesRepository.saveAll(List.of(size, color));
    }

    @AfterEach
    void clearData(){
        productsRepository.deleteAll();
        categoriesRepository.deleteAll();
        optionTypesRepository.deleteAll();
    }

    @Test
    void test(){
        log.info("clothes = {}", clothes.getId());
        log.info("T_shirt = {}", T_shirt.getId());

        log.info("XL = {}", xl.getId());
        log.info("BLUE = {}", blue.getId());
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
                        VariantsResponseDto::getSku,
                        VariantsResponseDto::getPrice,
                        VariantsResponseDto::getStockQuantity,
                        VariantsResponseDto::getDiscountValue
                )
                .containsExactlyInAnyOrder(
                        tuple("TS-M-BLUE", 29000, 30, 10)
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

    private VariantsRequestDto buildVariant(Long... optionValueIds){
        return new VariantsRequestDto(
                "TS-M-BLUE",
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