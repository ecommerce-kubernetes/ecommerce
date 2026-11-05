package com.example.product_service.docs.product;

import com.example.product_service.docs.RestDocsSupport;
import com.example.product_service.product.adapter.in.web.AdminProductController;
import com.example.product_service.product.adapter.in.web.dto.request.*;
import com.example.product_service.product.application.service.ProductCommandService;
import com.example.product_service.product.application.service.dto.command.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static com.example.product_service.docs.descriptor.ProductDescriptor.*;
import static com.example.product_service.product.fixture.ProductRequestFixture.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminProductControllerDocsTest extends RestDocsSupport {

    private ProductCommandService productCommandService = Mockito.mock(ProductCommandService.class);

    @Override
    protected Object initController() {
        return new AdminProductController(productCommandService);
    }

    @Test
    @DisplayName("상품을 생성한다")
    void createProduct() throws Exception {
        //given
        CreateProductRequest request = anCreateProductRequest().build();
        Long productId = 1L;
        HttpHeaders authHeader = createAuthHeader("ROLE_ADMIN");
        given(productCommandService.createProduct(any(CreateProductCommand.class))).willReturn(productId);
        //when
        //then
        mockMvc.perform(post("/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .headers(authHeader))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(document(
                        "admin/products/create",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(AUTH_HEADER),
                        requestFields(createProductRequest()),
                        responseFields(createProductResponse())
                ));
    }

    @Test
    @DisplayName("상품을 수정한다")
    void updateProduct() throws Exception {
        //given
        Long productId = 1L;
        UpdateProductRequest request = anUpdateProductRequest().build();
        HttpHeaders authHeader = createAuthHeader("ROLE_ADMIN");
        given(productCommandService.updateProduct(any(UpdateProductCommand.class))).willReturn(productId);
        //when
        //then
        mockMvc.perform(put("/admin/products/{productId}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .headers(authHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "admin/products/update",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(AUTH_HEADER),
                        pathParameters(parameterWithName("productId").description("수정할 상품 ID")),
                        requestFields(updateProductRequest()),
                        responseFields(updateProductResponse())
                ));
    }

    @Test
    @DisplayName("상품을 삭제한다")
    void deleteProduct() throws Exception {
        //given
        Long productId = 1L;
        HttpHeaders authHeader = createAuthHeader("ROLE_ADMIN");
        willDoNothing().given(productCommandService).deleteProduct(anyLong());
        //when
        //then
        mockMvc.perform(delete("/admin/products/{productId}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(authHeader))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(document(
                        "admin/products/delete",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(AUTH_HEADER),
                        pathParameters(parameterWithName("productId").description("삭제할 상품 ID"))
                ));
    }

    @Test
    @DisplayName("상품 옵션을 등록한다")
    void registerProductOptions() throws Exception {
        //given
        Long productId = 1L;
        RegisterProductOptionRequest request = anRegisterProductOptionRequest().build();
        HttpHeaders authHeader = createAuthHeader("ROLE_ADMIN");
        given(productCommandService.registerProductOptions(any(RegisterProductOptionCommand.class))).willReturn(productId);
        //when
        //then
        mockMvc.perform(put("/admin/products/{productId}/options", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .headers(authHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "admin/products/options",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(AUTH_HEADER),
                        pathParameters(parameterWithName("productId").description("옵션을 등록할 상품 ID")),
                        requestFields(registerProductOptionsRequest()),
                        responseFields(registerProductOptionsResponse())
                ));
    }

    @Test
    @DisplayName("상품 변형을 추가한다")
    void addProductVariants() throws Exception {
        //given
        Long productId = 1L;
        AddProductVariantRequest request = anAddProductVariantRequest().build();
        HttpHeaders authHeader = createAuthHeader("ROLE_ADMIN");
        given(productCommandService.addProductVariants(any(AddProductVariantCommand.class))).willReturn(productId);
        //when
        //then
        mockMvc.perform(post("/admin/products/{productId}/variants", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .headers(authHeader))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(document(
                        "admin/products/variants",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(AUTH_HEADER),
                        pathParameters(parameterWithName("productId").description("변형을 추가할 상품 ID")),
                        requestFields(addProductVariantsRequest()),
                        responseFields(addProductVariantsResponse())
                ));
    }

    @Test
    @DisplayName("상품 이미지를 추가한다")
    void addProductImages() throws Exception {
        //given
        Long productId = 1L;
        AddProductImageRequest request = anAddProductImageRequest().build();
        HttpHeaders authHeader = createAuthHeader("ROLE_ADMIN");
        given(productCommandService.addProductImages(any(AddProductImageCommand.class))).willReturn(productId);
        //when
        //then
        mockMvc.perform(put("/admin/products/{productId}/images", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .headers(authHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "admin/products/images",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(AUTH_HEADER),
                        pathParameters(parameterWithName("productId").description("이미지를 추가할 상품 ID")),
                        requestFields(addProductImagesRequest()),
                        responseFields(addProductImagesResponse())
                ));
    }

    @Test
    @DisplayName("상품 설명 이미지를 추가한다")
    void addProductDescriptionImages() throws Exception {
        //given
        Long productId = 1L;
        AddProductDescriptionImageRequest request = anAddProductDescriptionImageRequest().build();
        HttpHeaders authHeader = createAuthHeader("ROLE_ADMIN");
        given(productCommandService.addProductDescriptionImages(any(AddProductDescriptionImageCommand.class))).willReturn(productId);
        //when
        //then
        mockMvc.perform(put("/admin/products/{productId}/description-images", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .headers(authHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "admin/products/description-images",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(AUTH_HEADER),
                        pathParameters(parameterWithName("productId").description("설명 이미지를 추가할 상품 ID")),
                        requestFields(addProductDescriptionImagesRequest()),
                        responseFields(addProductDescriptionImagesResponse())
                ));
    }
}
