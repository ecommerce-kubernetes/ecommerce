package com.example.product_service.controller;

import com.example.product_service.controller.util.SortFieldValidator;
import com.example.product_service.dto.request.ProductRequestDto;
import com.example.product_service.dto.request.StockQuantityRequestDto;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.ProductResponseDto;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ProductController.class)
@Import(SortFieldValidator.class)
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

        String requestBody = mapper.writeValueAsString(productRequestDto);
        ResultActions perform = mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        perform
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

    @Test
    @DisplayName("product Id 조회 테스트")
    void getProductByIdTest() throws Exception {
        ProductResponseDto productResponseDto = createDefaultProductResponseDto();

        when(productService.getProductDetails(any(Long.class))).thenReturn(productResponseDto);
        ResultActions perform = mockMvc.perform(get("/products/1")
                .contentType(MediaType.APPLICATION_JSON));

        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("테스트 상품 이름"))
                .andExpect(jsonPath("$.description").value("테스트 상품 설명"))
                .andExpect(jsonPath("$.price").value(10000))
                .andExpect(jsonPath("$.stockQuantity").value(50))
                .andExpect(jsonPath("$.categoryId").value(1L));
    }

    @ParameterizedTest
    @CsvSource({
            "'', ''",
            "1, ''",
            "'', 사과",
            "1, 사과"
    })
    @DisplayName("상품 리스트 조회 테스트")
    void getAllProductsTest(String categoryId, String name) throws Exception {

        List<ProductResponseDto> allProductList = new ArrayList<>();
        allProductList.add(new ProductResponseDto(1L, "사과", "사과 3EA", 5000, 50, 1L));
        allProductList.add(new ProductResponseDto(2L, "바나나", "바나나 3EA", 5000, 50, 1L));
        allProductList.add(new ProductResponseDto(3L, "파인애플", "파인애플 5EA", 6000, 40, 1L));
        allProductList.add(new ProductResponseDto(4L, "포도", "포도 6EA",10000, 40, 1L));
        allProductList.add(new ProductResponseDto(5L, "아이폰 16", "애플 아이폰 16", 1250000, 50, 2L));

        List<ProductResponseDto> filteredProductResponseDto = allProductList.stream()
                .filter(product -> {
                    if (!categoryId.isEmpty()) {
                        return product.getCategoryId().toString().equals(categoryId);
                    }
                    return true;
                })
                .filter(product -> {
                    if (!name.isEmpty()) {
                        return product.getName().toLowerCase().contains(name.toLowerCase());
                    }
                    return true;
                })
                .toList();

        PageDto<ProductResponseDto> pageDto = new PageDto<>(
                filteredProductResponseDto,
                0,
                1,
                10,
                filteredProductResponseDto.size()
        );

        when(productService.getProductList(any(Pageable.class) , nullable(Long.class), nullable(String.class)))
                .thenReturn(pageDto);

        MockHttpServletRequestBuilder requestBuilder = get("/products")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "id")
                .param("direction", "asc")
                .contentType(MediaType.APPLICATION_JSON);

        if(!categoryId.isEmpty()){
            requestBuilder.param("categoryId", categoryId);
        }
        if (!name.isEmpty()) {
            requestBuilder.param("name", name);
        }
        ResultActions perform = mockMvc.perform(requestBuilder);

        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPage").value(1))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalElement").value(filteredProductResponseDto.size()));

        for (int i = 0; i < filteredProductResponseDto.size(); i++) {
            ProductResponseDto expected = filteredProductResponseDto.get(i);
            perform
                    .andExpect(jsonPath("$.content[" + i + "].id").value(expected.getId()))
                    .andExpect(jsonPath("$.content[" + i + "].name").value(expected.getName()))
                    .andExpect(jsonPath("$.content[" + i + "].description").value(expected.getDescription()))
                    .andExpect(jsonPath("$.content[" + i + "].price").value(expected.getPrice()))
                    .andExpect(jsonPath("$.content[" + i + "].stockQuantity").value(expected.getStockQuantity()))
                    .andExpect(jsonPath("$.content[" + i + "].categoryId").value(expected.getCategoryId()));
        }
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