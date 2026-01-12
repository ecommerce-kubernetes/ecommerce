package com.example.product_service.docs.product;

import com.example.product_service.api.product.controller.ProductController;
import com.example.product_service.api.product.controller.dto.ProductCreateRequest;
import com.example.product_service.api.product.service.ProductService;
import com.example.product_service.api.product.service.dto.command.ProductCreateCommand;
import com.example.product_service.api.product.service.dto.result.ProductCreateResponse;
import com.example.product_service.docs.RestDocsSupport;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProductControllerDocsTest extends RestDocsSupport {
    ProductService productService = Mockito.mock(ProductService.class);
    @Override
    protected Object initController() {
        return new ProductController(productService);
    }

    @Test
    @DisplayName("상품을 생성한다")
    void createProduct() throws Exception {
        //given
        ProductCreateRequest request = createProductCreateRequest().build();
        ProductCreateResponse response = createProductCreateResponse().build();
        given(productService.createProduct(any(ProductCreateCommand.class)))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(print())
                .andDo(
                        document("create-product",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),

                                requestHeaders(
                                        headerWithName("X-User-Id").description(USER_ID_HEADER_DESCRIPTION).optional(),
                                        headerWithName("X-User-Role").description(USER_ROLE_HEADER_DESCRIPTION).optional()
                                ),

                                requestFields(
                                        fieldWithPath("name").description("상품 이름").optional(),
                                        fieldWithPath("categoryId").description("카테고리 Id").optional(),
                                        fieldWithPath("description").description("상품 설명"),
                                        fieldWithPath("basePrice").description("임시 가격").optional()
                                ),

                                responseFields(
                                        fieldWithPath("productId").description("상품 Id"),
                                        fieldWithPath("name").description("상품 이름"),
                                        fieldWithPath("categoryId").description("카테고리 Id"),
                                        fieldWithPath("basePrice")
                                )

                        )
                );
    }

    private ProductCreateRequest.ProductCreateRequestBuilder createProductCreateRequest(){
        return ProductCreateRequest.builder()
                .name("상품")
                .categoryId(1L)
                .description("상품 설명")
                .displayPrice(3000L);
    }

    private ProductCreateResponse.ProductCreateResponseBuilder createProductCreateResponse(){
        return ProductCreateResponse.builder()
                .productId(1L)
                .name("상품")
                .categoryId(1L)
                .basePrice(3000L)
                .status("PREPARING")
                .createdAt(LocalDateTime.now().toString());
    }
}
