package com.example.product_service.controller;

import com.example.product_service.common.advice.dto.DetailError;
import com.example.product_service.controller.util.ControllerResponseValidator;
import com.example.product_service.controller.util.SortFieldValidator;
import com.example.product_service.dto.request.product.ProductRequestDto;
import com.example.product_service.dto.request.product.VariantsRequestDto;
import com.example.product_service.dto.response.product.ProductImageDto;
import com.example.product_service.dto.response.product.ProductResponseDto;
import com.example.product_service.dto.response.product.VariantsResponseDto;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ProductController.class)
@Import({SortFieldValidator.class, ControllerResponseValidator.class})
@Slf4j
class ProductControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ControllerResponseValidator validator;
    @MockitoBean
    ProductService productService;
    ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("상품 저장 테스트")
    void createProductTest_success() throws Exception {
        //Option Type Id
        Long colorId = 1L; Long sizeId = 2L;
        //Option Value Id
        Long xlId = 1L; Long blueId = 2L;
        //Category Id
        Long clothesId = 1L;

        String requestUrl = "/products";

        //RequestDto
        VariantsRequestDto variantRequest = buildVariantRequest(xlId, blueId);
        ProductRequestDto requestDto = buildProductRequest(clothesId, List.of(colorId, sizeId), List.of(variantRequest));

        //ResponseDto
        VariantsResponseDto variantResponse = buildVariantResponse(xlId, blueId);
        ProductResponseDto productResponse =
                buildProductResponse(clothesId, List.of(colorId, sizeId), List.of(variantResponse));

        //ProductsService Mocking
        when(productService.saveProduct(any(ProductRequestDto.class))).thenReturn(productResponse);

        String content = mapper.writeValueAsString(requestDto);
        ResultActions perform = mockMvc.perform(post(requestUrl).contentType(MediaType.APPLICATION_JSON)
                .content(content));


        //verifying
        perform.andExpect(status().isCreated());
        validator.validResponse(perform, ProductResponseDto.class, productResponse);
    }

    @ParameterizedTest
    @MethodSource("provideCreateProductInvalidRequestDto")
    @DisplayName("상품 저장_필드 검증 테스트")
    void createProductTest_InvalidRequestField(ProductRequestDto requestDto, int expectedStatus,
                                               String expectedError, String expectedMessage,
                                               String errorField, String expectedValidMessage) throws Exception {

        String content = mapper.writeValueAsString(requestDto);

        String requestUrl = "/products";
        ResultActions perform = mockMvc.perform(post(requestUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content));

        validator.validInvalidFields(perform,
                expectedStatus,
                expectedError,
                expectedMessage,
                List.of(new DetailError(errorField, expectedValidMessage)),
                requestUrl);
    }

    @ParameterizedTest
    @MethodSource("provideCreateProductExceptionRequest")
    @DisplayName("상품 저장_예외 테스트")
    void createProduct_Exception(int expectedStatus, String expectedError, Exception ex) throws Exception {
        Long colorId = 1L; Long sizeId = 2L;
        //Option Value Id
        Long xlId = 1L; Long blueId = 2L;
        //Category Id
        Long clothesId = 1L;
        String requestUrl = "/products";

        VariantsRequestDto variantsRequestDto = buildVariantRequest(xlId, blueId);
        ProductRequestDto requestDto =
                buildProductRequest(clothesId, List.of(clothesId, sizeId), List.of(variantsRequestDto));

        when(productService.saveProduct(any(ProductRequestDto.class)))
                .thenThrow(ex);

        String content = mapper.writeValueAsString(requestDto);
        ResultActions perform =
                mockMvc.perform(post(requestUrl).contentType(MediaType.APPLICATION_JSON).content(content));

        validator.verifyErrorResponse(perform, expectedStatus, expectedError, ex.getMessage(), requestUrl);
    }

    private VariantsRequestDto buildVariantRequest(Long... optionValueIds){
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

    private ProductResponseDto buildProductResponse(Long categoryId, List<Long> optionTypeIds, List<VariantsResponseDto> variants){
        return new ProductResponseDto(
                1L,
                "나이키 티셔츠",
                "나이키 반팔티 아이콘 NSW 퓨추라 스우시 반팔 티셔츠",
                categoryId,
                LocalDateTime.of(2025,5,25,13,20),
                LocalDateTime.of(2025,5,25,13,20),
                List.of(new ProductImageDto(1L, "http://test1.jpg", 0),
                        new ProductImageDto(2L, "http://test2.jpg", 1)),
                0,
                0,
                optionTypeIds,
                variants
        );
    }

    private VariantsResponseDto buildVariantResponse(Long... optionValueIds){
        return new VariantsResponseDto(
                1L,
                "TS-XL-BLUE",
                29000,
                30,
                10,
                List.of(optionValueIds)
        );
    }

    static Stream<Arguments> provideCreateProductInvalidRequestDto(){
        String name = "나이키 티셔츠";
        String description = "나이키 반팔티 아이콘 NSW 퓨추라 스우시 반팔 티셔츠";
        //카테고리 ID
        Long clothes = 1L;
        List<String> imageUrls = List.of("http://test1.jpg", "http://test2.jpg");
        //OptionType
        Long color = 1L; Long size = 2L;
        //OptionValue
        Long blue = 1L; Long m = 2L;

        //
        String expectedError = "BadRequest";
        String expectedMessage = "Validation Error";
        return Stream.of(
                //이름 필드 검증
                Arguments.of(
                        new ProductRequestDto("", description, clothes, imageUrls, List.of(color, size),
                                List.of(
                                        new VariantsRequestDto(
                                                29000,
                                                30,
                                                10,
                                                List.of(blue, m))
                                )
                        ),
                        HttpStatus.SC_BAD_REQUEST,
                        expectedError,
                        expectedMessage,
                        "name",
                        "Product name is required"
                ),
                //description 필드 검증
                Arguments.of(
                        new ProductRequestDto(name, "", clothes, imageUrls, List.of(color, size),
                                List.of(
                                        new VariantsRequestDto(
                                                29000,
                                                30,
                                                10,
                                                List.of(blue, m))
                                )
                        ),
                        HttpStatus.SC_BAD_REQUEST,
                        expectedError,
                        expectedMessage,
                        "description",
                        "Product description is required"
                ),
                //CategoryId
                Arguments.of(
                        new ProductRequestDto(name, description, null, imageUrls, List.of(color, size),
                                List.of(
                                        new VariantsRequestDto(
                                                29000,
                                                30,
                                                10,
                                                List.of(blue, m))
                                )
                        ),
                        HttpStatus.SC_BAD_REQUEST,
                        expectedError,
                        expectedMessage,
                        "categoryId",
                        "Product categoryId is required"
                ),
                //imageUrl
                Arguments.of(
                        new ProductRequestDto(name, description, clothes, null, List.of(color, size),
                                List.of(
                                        new VariantsRequestDto(
                                                29000,
                                                30,
                                                10,
                                                List.of(blue, m))
                                )
                        ),
                        HttpStatus.SC_BAD_REQUEST,
                        expectedError,
                        expectedMessage,
                        "imageUrls",
                        "At least one image URL is required"
                ),
                Arguments.of(
                        new ProductRequestDto(name, description, clothes, List.of("asdf"), List.of(color, size),
                                List.of(
                                        new VariantsRequestDto(
                                                29000,
                                                30,
                                                10,
                                                List.of(blue, m))
                                )
                        ),
                        HttpStatus.SC_BAD_REQUEST,
                        expectedError,
                        expectedMessage,
                        "imageUrls[0]",
                        "Invalid image URL"
                ),
                //variant == null;
                Arguments.of(
                        new ProductRequestDto(name, description, clothes, imageUrls, List.of(color, size),
                                null
                        ),
                        HttpStatus.SC_BAD_REQUEST,
                        expectedError,
                        expectedMessage,
                        "variants",
                        "Variants is not empty"
                ),
                //sku
                Arguments.of(
                        new ProductRequestDto(name, description, clothes, imageUrls, List.of(color, size),
                                List.of(
                                        new VariantsRequestDto(
                                                0,
                                                30,
                                                10,
                                                List.of(blue, m))
                                )
                        ),
                        HttpStatus.SC_BAD_REQUEST,
                        expectedError,
                        expectedMessage,
                        "variants[0].price",
                        "price must be greater than 0"
                ),
                Arguments.of(
                        new ProductRequestDto(name, description, clothes, imageUrls, List.of(color, size),
                                List.of(
                                        new VariantsRequestDto(
                                                1000000000,
                                                30,
                                                10,
                                                List.of(blue, m))
                                )
                        ),
                        HttpStatus.SC_BAD_REQUEST,
                        expectedError,
                        expectedMessage,
                        "variants[0].price",
                        "price must be less than 50000000"
                ),
                Arguments.of(
                        new ProductRequestDto(name, description, clothes, imageUrls, List.of(color, size),
                                List.of(
                                        new VariantsRequestDto(
                                                29000,
                                                0,
                                                10,
                                                List.of(blue, m))
                                )
                        ),
                        HttpStatus.SC_BAD_REQUEST,
                        expectedError,
                        expectedMessage,
                        "variants[0].stockQuantity",
                        "stockQuantity must be greater than 0"
                ),
                Arguments.of(
                        new ProductRequestDto(name, description, clothes, imageUrls, List.of(color, size),
                                List.of(
                                        new VariantsRequestDto(
                                                29000,
                                                20000,
                                                10,
                                                List.of(blue, m))
                                )
                        ),
                        HttpStatus.SC_BAD_REQUEST,
                        expectedError,
                        expectedMessage,
                        "variants[0].stockQuantity",
                        "stockQuantity must be less than 1000"
                )

        );
    }

    static Stream<Arguments> provideCreateProductExceptionRequest(){
        return Stream.of(
                Arguments.of(
                        HttpStatus.SC_NOT_FOUND,
                        "NotFound",
                        new NotFoundException("Not Found Category")
                ),
                Arguments.of(
                        HttpStatus.SC_BAD_REQUEST,
                        "BadRequest",
                        new BadRequestException("Category must be lowest level")
                ),
                Arguments.of(
                        HttpStatus.SC_NOT_FOUND,
                        "NotFound",
                        new NotFoundException("Invalid OptionType Ids : [999]")
                ),
                Arguments.of(
                        HttpStatus.SC_NOT_FOUND,
                        "NotFound",
                        new NotFoundException("Invalid OptionValue Ids : [999]")
                )
        );
    }

}