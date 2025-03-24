package com.example.product_service.controller;

import com.example.product_service.dto.request.ProductRequestDto;
import com.example.product_service.dto.response.ProductResponseDto;
import com.example.product_service.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ProductController.class)
@Slf4j
class ProductControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    ProductService productService;


    ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("product 생성 테스트")
    void createProductTest() throws Exception {
        //RequestBody
        ProductRequestDto productRequestDto = new ProductRequestDto(
                "테스트 상품 이름",
                "테스트 상품 설명",
                10000,
                50,
                1L
        );
        //ResponseBody
        ProductResponseDto productResponseDto = new ProductResponseDto(
                1L,
                productRequestDto.getName(),
                productRequestDto.getDescription(),
                productRequestDto.getPrice(),
                productRequestDto.getStockQuantity(),
                productRequestDto.getCategoryId()
        );
        /*
         * 목 productService 객체 saveProduct 호출
         * input -> ProductRequestDto.class
         * output -> productResponseDto
         */
        when(productService.saveProduct(any(ProductRequestDto.class))).thenReturn(productResponseDto);


        String jsonRequestBody = mapper.writeValueAsString(productRequestDto);
        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(productResponseDto.getId()))
                .andExpect(jsonPath("$.name").value(productResponseDto.getName()))
                .andExpect(jsonPath("$.description").value(productResponseDto.getDescription()))
                .andExpect(jsonPath("$.price").value(productResponseDto.getPrice()))
                .andExpect(jsonPath("$.stockQuantity").value(productResponseDto.getStockQuantity()))
                .andExpect(jsonPath("$.categoryId").value(productResponseDto.getCategoryId()));

    }

    @Test
    @DisplayName("product 생성 테스트 - 이름 BadRequest")
    void createProductTest_BadRequestName() throws Exception {
        ProductRequestDto productRequestDto = new ProductRequestDto(
                "",
                "테스트 상품 설명",
                10000,
                50,
                1L
        );
        //ResponseBody
        ProductResponseDto productResponseDto = new ProductResponseDto(
                1L,
                productRequestDto.getName(),
                productRequestDto.getDescription(),
                productRequestDto.getPrice(),
                productRequestDto.getStockQuantity(),
                productRequestDto.getCategoryId()
        );

        when(productService.saveProduct(any(ProductRequestDto.class))).thenReturn(productResponseDto);
        String jsonRequestBody = mapper.writeValueAsString(productRequestDto);
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BadRequest"))
                .andExpect(jsonPath("$.message").value("Validation Error"))
                .andExpect(jsonPath("$.errors[*].fieldName").value(hasItem("name")))
                .andExpect(jsonPath("$.errors[*].message").value(hasItem("Product name is required")))
                .andExpect(jsonPath("$.path").value("/products"));
    }
}