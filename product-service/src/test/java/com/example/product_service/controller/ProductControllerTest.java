package com.example.product_service.controller;

import com.example.product_service.dto.request.ProductRequestDto;
import com.example.product_service.dto.request.StockQuantityRequestDto;
import com.example.product_service.dto.response.ProductResponseDto;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
        ProductRequestDto productRequestDto = createDefaultProductRequestDto();
        //ResponseBody
        ProductResponseDto productResponseDto = createDefaultProductResponseDto();
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

    @ParameterizedTest(name = "{1} 필드 => {2}")
    @MethodSource("provideInvalidProductRequests")
    @DisplayName("product 생성 테스트 - 입력값 검증 테스트")
    void createProductTest_InvalidProductRequestDto(ProductRequestDto invalidRequestDto, String expectedField, String expectedMessage)
            throws Exception {
        String jsonRequestBody = mapper.writeValueAsString(invalidRequestDto);
        ResultActions perform = mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequestBody));

        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BadRequest"))
                .andExpect(jsonPath("$.message").value("Validation Error"))
                .andExpect(jsonPath("$.errors[*].fieldName").value(hasItem(expectedField)))
                .andExpect(jsonPath("$.errors[*].message").value(hasItem(expectedMessage)))
                .andExpect(jsonPath("$.path").value("/products"));
    }

    @Test
    @DisplayName("product 생성 테스트 - 없는 상품 카테고리 입력시")
    void createProductTest_NotFoundCategory() throws Exception {
        ProductRequestDto productRequestDto = createDefaultProductRequestDto();
        when(productService.saveProduct(any(ProductRequestDto.class))).thenThrow(new NotFoundException("Not Found Category"));

        String jsonRequestBody = mapper.writeValueAsString(productRequestDto);
        ResultActions perform = mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequestBody));

        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NotFound"))
                .andExpect(jsonPath("$.message").value("Not Found Category"))
                .andExpect(jsonPath("$.path").value("/products"));
    }

    @Test
    @DisplayName("product 삭제 테스트")
    void removeProductTest() throws Exception {
        doNothing().when(productService).deleteProduct(any(Long.class));

        ResultActions perform = mockMvc.perform(delete("/products/1")
                .contentType(MediaType.APPLICATION_JSON));

        perform
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("product 삭제 테스트 - 상품을 찾을 수 없을때")
    void removeProductTest_NotFoundProduct() throws Exception {
        doThrow(new NotFoundException("Not Found Product"))
                .when(productService).deleteProduct(any(Long.class));

        ResultActions perform = mockMvc.perform(delete("/products/1")
                .contentType(MediaType.APPLICATION_JSON));

        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NotFound"))
                .andExpect(jsonPath("$.message").value("Not Found Product"))
                .andExpect(jsonPath("$.path").value("/products/1"));
    }

    @Test
    @DisplayName("product stockQuantity 변경 테스트")
    void updateProductStockQuantityTest() throws Exception {
        StockQuantityRequestDto stockQuantityRequestDto = new StockQuantityRequestDto(40);
        ProductResponseDto productResponseDto = createDefaultProductResponseDto();
        productResponseDto.setStockQuantity(40);
        when(productService.modifyStockQuantity(any(Long.class),any(StockQuantityRequestDto.class)))
                .thenReturn(productResponseDto);

        String requestBody = mapper.writeValueAsString(stockQuantityRequestDto);

        ResultActions perform = mockMvc.perform(patch("/products/1/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productResponseDto.getId()))
                .andExpect(jsonPath("$.name").value(productResponseDto.getName()))
                .andExpect(jsonPath("$.description").value(productResponseDto.getDescription()))
                .andExpect(jsonPath("$.price").value(productResponseDto.getPrice()))
                .andExpect(jsonPath("$.stockQuantity").value(productResponseDto.getStockQuantity()))
                .andExpect(jsonPath("$.categoryId").value(productResponseDto.getCategoryId()));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidStockRequests")
    @DisplayName("product stockQuantity 변경 테스트 - 입력값 검증 테스트")
    void updateProductStockQuantityTest_invalidStockQuantityRequestDto
            (StockQuantityRequestDto stockQuantityRequestDto, String expectedField, String expectedMessage) throws Exception {
        String requestBody = mapper.writeValueAsString(stockQuantityRequestDto);
        log.info("{}",requestBody);
        ResultActions perform = mockMvc.perform(patch("/products/1/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BadRequest"))
                .andExpect(jsonPath("$.message").value("Validation Error"))
                .andExpect(jsonPath("$.errors[*].fieldName").value(hasItem(expectedField)))
                .andExpect(jsonPath("$.errors[*].message").value(hasItem(expectedMessage)))
                .andExpect(jsonPath("$.path").value("/products/1/stock"));
    }


    private ProductRequestDto createDefaultProductRequestDto(){
        return new ProductRequestDto(
                    "테스트 상품 이름",
                    "테스트 상품 설명",
                    10000,
                    50,
                    1L
                );
    }
    private ProductResponseDto createDefaultProductResponseDto(){
        ProductRequestDto request = createDefaultProductRequestDto();
        return new ProductResponseDto(
                1L,
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getStockQuantity(),
                request.getCategoryId()
        );
    }

    private static Stream<Arguments> provideInvalidProductRequests(){
        return Stream.of(
                Arguments.of(
                        //이름이 비어있는 경우
                        new ProductRequestDto("", "테스트 상품 설명",10000, 50, 1L),
                        "name", //오류 필드
                        "Product name is required" //오류 메시지
                ),
                Arguments.of(
                        //설명이 비어있는 경우
                        new ProductRequestDto("테스트 상품", "", 10000, 50, 1L),
                        "description",
                        "Product description is required"
                ),
                Arguments.of(
                        //상품 가격이 0원 미만일때
                        new ProductRequestDto("테스트 상품", "테스트 상품 설명", -1, 50, 1L),
                        "price",
                        "Product price must not be less than 0"
                ),
                Arguments.of(
                        //상품 가격이 10000000 이상일때
                        new ProductRequestDto("테스트 상품", "테스트 상품 설명", 10000001, 50, 1L),
                        "price",
                        "Product price must not be greater than 10,000,000"
                ),
                Arguments.of(
                        //상품 개수가 0개 이하일때
                        new ProductRequestDto("테스트 상품", "테스트 상품 설명", 10000, -1, 1L),
                        "stockQuantity",
                        "Product stockQuantity must not be less than 0"
                ),
                Arguments.of(
                        //상품 개수가 100개 이상일때
                        new ProductRequestDto("테스트 상품", "테스트 상품 설명", 10000, 101, 1L),
                        "stockQuantity",
                        "Product stockQuantity must not be greater than 100"
                ),
                Arguments.of(
                        //상품 카테고리가 없는 경우
                        new ProductRequestDto("테스트 상품", "테스트 상품 설명", 10000, 101, null),
                        "categoryId",
                        "Product categoryId is required"
                )
        );
    }
    private static Stream<Arguments> provideInvalidStockRequests() {
        return Stream.of(
                Arguments.of(
                        new StockQuantityRequestDto(-1),
                        "updateStockQuantity",
                        "Product stockQuantity must not be less than 0"
                ),
                Arguments.of(
                        new StockQuantityRequestDto(101),
                        "updateStockQuantity",
                        "Product stockQuantity must not be greater than 100"
                )
        );
    }
}