package com.example.product_service.service;

import com.example.product_service.dto.request.ImageOrderRequestDto;
import com.example.product_service.dto.response.product.ProductImageDto;
import com.example.product_service.dto.response.product.ProductResponseDto;
import com.example.product_service.entity.Categories;
import com.example.product_service.entity.ProductImages;
import com.example.product_service.entity.Products;
import com.example.product_service.repository.CategoriesRepository;
import com.example.product_service.repository.ProductsRepository;
import com.example.product_service.service.client.ImageClientService;
import lombok.extern.slf4j.Slf4j;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static org.assertj.core.api.Assertions.anyOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@Slf4j
class ProductImageServiceImplTest {
    @Autowired
    ProductImageService productImageService;
    @Autowired
    ProductsRepository productsRepository;
    @Autowired
    CategoriesRepository categoriesRepository;
    @MockitoBean
    ImageClientService imageClientService;

    Categories clothes;
    Categories T_shirt;
    Categories sweatShirt;

    @BeforeEach
    void initData() {
        clothes = new Categories("의류", null);
        T_shirt = new Categories("티셔츠", null);
        sweatShirt = new Categories("맨투맨", null);
        clothes.addChild(T_shirt);
        clothes.addChild(sweatShirt);
        categoriesRepository.save(clothes);
    }
    @AfterEach
    void clearData(){
        productsRepository.deleteAll();
        categoriesRepository.deleteAll();
    }

    @Test
    @DisplayName("상품 이미지 삭제")
    @Transactional
    void deleteImageTest(){
        Products product = new Products("오버핏 반팔티", "오버핏 반팔티 아이콘 NSW 퓨추라 스우시 반팔 티셔츠", T_shirt);
        product.addImage("http://test1.jpg", 0);
        product.addImage("http://test2.jpg",1);
        productsRepository.save(product);

        Long id = product.getImages().get(0).getId();

        doNothing().when(imageClientService).deleteImage(any());
        productImageService.deleteImage(id);
        assertThat(product.getImages().size()).isEqualTo(1);

    }

    @Test
    @DisplayName("상품 이미지 순서 변경")
    @Transactional
    void imgSwapOrderTest(){
        Products product = new Products("오버핏 반팔티", "오버핏 반팔티 아이콘 NSW 퓨추라 스우시 반팔 티셔츠", T_shirt);
        product.addImage("http://test1.jpg", 0);
        product.addImage("http://test2.jpg", 1);
        productsRepository.save(product);
        ProductImages targetImage = product.getImages().stream().filter(image -> image.getSortOrder() == 1).findFirst()
                .orElseThrow();
        ImageOrderRequestDto requestDto = new ImageOrderRequestDto(0);

        ProductResponseDto responseDto = productImageService.imgSwapOrder(targetImage.getId(), requestDto);

        ProductImageDto productImageDto = responseDto.getImages().stream().filter(image -> Objects.equals(image.getId(), targetImage.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(productImageDto.getSortOrder()).isEqualTo(0);
    }
}