package com.example.product_service.docs.product;

import com.example.product_service.docs.RestDocsSupport;
import com.example.product_service.product.adapter.in.web.ProductController;
import com.example.product_service.product.application.service.ProductQueryService;
import com.example.product_service.product.application.service.dto.result.ProductDetailResult;
import com.example.product_service.product.application.service.dto.result.ProductSummaryResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static com.example.product_service.docs.descriptor.ProductDescriptor.*;
import static com.example.product_service.product.fixture.ProductResultFixture.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductControllerDocsTest extends RestDocsSupport {

    private ProductQueryService productQueryService = Mockito.mock(ProductQueryService.class);

    @Override
    protected Object initController() {
        return new ProductController(productQueryService);
    }

    @Test
    @DisplayName("상품 목록을 조회한다")
    void getProducts() throws Exception {
        //given
        ProductSummaryResult summary = aProductSummaryResult().build();
        Page<ProductSummaryResult> results = new PageImpl<>(List.of(summary), PageRequest.of(0, 20), 1);
        given(productQueryService.getProducts(any())).willReturn(results);
        //when
        //then
        mockMvc.perform(get("/products")
                        .param("categoryId", "1")
                        .param("name", "나이키")
                        .param("rating", "3")
                        .param("sort", "latest")
                        .param("page", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "products/list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        queryParameters(productSearchParams()),
                        responseFields(productListResponse())
                ));
    }

    @Test
    @DisplayName("상품 상세를 조회한다")
    void getProduct() throws Exception {
        //given
        ProductDetailResult detail = aProductDetailResult().build();
        given(productQueryService.getProduct(anyLong())).willReturn(detail);
        //when
        //then
        mockMvc.perform(get("/products/{productId}", detail.productId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "products/detail",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(parameterWithName("productId").description("조회할 상품 ID")),
                        responseFields(productDetailResponse())
                ));
    }
}
