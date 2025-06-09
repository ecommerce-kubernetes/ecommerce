package com.example.product_service.service;

import com.example.product_service.dto.request.product.CreateVariantsRequestDto;
import com.example.product_service.dto.request.product.VariantsRequestDto;
import com.example.product_service.dto.response.product.ProductResponseDto;
import com.example.product_service.dto.response.product.VariantsResponseDto;
import com.example.product_service.entity.*;
import com.example.product_service.repository.CategoriesRepository;
import com.example.product_service.repository.OptionTypesRepository;
import com.example.product_service.repository.ProductsRepository;
import com.example.product_service.repository.ReviewsRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class ProductVariantServiceImplTest {
    @Autowired
    ProductVariantService productVariantService;

    @Autowired
    ProductsRepository productsRepository;
    @Autowired
    CategoriesRepository categoriesRepository;
    @Autowired
    OptionTypesRepository optionTypesRepository;
    @Autowired
    ReviewsRepository reviewsRepository;

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


    Categories clothes;
    Categories T_shirt;
    Categories sweatShirt;

    OptionTypes size;
    OptionTypes color;

    OptionValues xl;
    OptionValues blue;
    OptionValues l;
    OptionValues red;

    @Test
    @DisplayName("상품 Variants  추가")
    @Transactional
    void addVariantsTest(){
        Products over = new Products("오버핏 반팔티", "오버핏 반팔티 아이콘 NSW 퓨추라 스우시 반팔 티셔츠", T_shirt);
        over.addProductOptionTypes(size, 0, true);
        over.addProductOptionTypes(color, 1, true);
        over.addProductVariants("TS-XL-BLUE", 3000, 30, 10, List.of(xl,blue));

        productsRepository.save(over);

        CreateVariantsRequestDto requestDto = new CreateVariantsRequestDto(
                List.of(
                        new VariantsRequestDto(5000, 30, 10, List.of(l.getId(), red.getId())
                        )));
        ProductResponseDto responseDto = productVariantService.addVariants(over.getId(), requestDto);

        assertThat(responseDto.getVariants().size()).isEqualTo(2);

        assertThat(responseDto.getVariants())
                .extracting(
                        VariantsResponseDto::getPrice,
                        VariantsResponseDto::getStockQuantity,
                        VariantsResponseDto::getDiscountValue,
                        VariantsResponseDto::getOptionValueId
                )
                .containsExactlyInAnyOrder(
                        tuple(3000, 30, 10, List.of(xl.getId(), blue.getId())),
                        tuple(5000, 30, 10, List.of(l.getId(), red.getId()))
                );
    }

    @Test
    @DisplayName("상품 Variants 삭제")
    @Transactional
    void deleteVariantTest(){
        Products over = new Products("오버핏 반팔티", "오버핏 반팔티 아이콘 NSW 퓨추라 스우시 반팔 티셔츠", T_shirt);
        over.addProductOptionTypes(size, 0, true);
        over.addProductOptionTypes(color, 1, true);
        over.addProductVariants("TS-XL-BLUE", 3000, 30, 10, List.of(xl,blue));
        over.addProductVariants("TS-L-RED", 5000, 20, 10, List.of(l,red));
        productsRepository.save(over);

        ProductVariants variants = over.getProductVariants()
                .stream().filter(variant -> variant.getSku().equals("TS-L-RED"))
                .findFirst().orElseThrow();

        productVariantService.deleteVariant(variants.getId());

        assertThat(over.getProductVariants().size()).isEqualTo(1);
        assertThat(over.getProductVariants())
                .extracting(
                        ProductVariants::getSku,
                        ProductVariants::getPrice,
                        ProductVariants::getStockQuantity,
                        ProductVariants::getDiscountValue
                )
                .containsExactlyInAnyOrder(
                        tuple("TS-XL-BLUE", 3000, 30, 10)
                );
    }

}