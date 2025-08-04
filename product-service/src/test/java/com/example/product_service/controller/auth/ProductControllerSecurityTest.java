package com.example.product_service.controller.auth;

import com.example.product_service.common.advice.CustomAccessDeniedHandler;
import com.example.product_service.common.advice.CustomAuthenticationEntryPoint;
import com.example.product_service.config.WebSecurity;
import com.example.product_service.controller.ProductController;
import com.example.product_service.dto.request.image.ImageRequest;
import com.example.product_service.dto.request.options.ProductOptionTypeRequest;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.product.UpdateProductBasicRequest;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
import com.example.product_service.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static com.example.product_service.controller.util.SecurityTestHelper.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(ProductController.class)
@Import({WebSecurity.class, CustomAccessDeniedHandler.class, CustomAuthenticationEntryPoint.class})
@AutoConfigureMockMvc
public class ProductControllerSecurityTest {

    private static final String BASE_PATH = "/products";
    private static final String PRODUCT_ID_PATH = "/products/1";
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    ProductService productService;

    @Test
    @DisplayName("상품 저장 테스트-인증 에러")
    void createProductTest_UnAuthorized() throws Exception {
        String jsonBody = toJson(createProductRequest());

        ResultActions perform = mockMvc.perform(post(BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody));

        verifyUnauthorizedResponse(perform, BASE_PATH);
    }

    @Test
    @DisplayName("상품 저장 테스트-권한 부족")
    void createProductTest_NoPermission() throws Exception {
        String jsonBody = toJson(createProductRequest());

        ResultActions perform = mockMvc.perform(post(BASE_PATH)
                .header(USER_ID_HEADER, 1L)
                .header(USER_ROLE_HEADER, USER_ROLE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody));

        verifyNoPermissionResponse(perform, BASE_PATH);
    }

    @Test
    @DisplayName("상품 기본 정보 수정 테스트-인증 에러")
    void updateBasicInfoTest_UnAuthorized() throws Exception {
        String jsonBody = toJson(createUpdateProductBasicRequest());

        ResultActions perform = mockMvc.perform(patch(PRODUCT_ID_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody));

        verifyUnauthorizedResponse(perform, PRODUCT_ID_PATH);
    }

    @Test
    @DisplayName("상품 기본 정보 수정 테스트-권한 부족")
    void updateBasicInfoTest_NoPermission() throws Exception {
        String jsonBody = toJson(createUpdateProductBasicRequest());

        ResultActions perform = mockMvc.perform(patch(PRODUCT_ID_PATH)
                        .header(USER_ID_HEADER, 1)
                        .header(USER_ROLE_HEADER, USER_ROLE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody));

        verifyNoPermissionResponse(perform, PRODUCT_ID_PATH);
    }

    @Test
    @DisplayName("상품 삭제 테스트-인증 에러")
    void deleteProductTest_UnAuthorized() throws Exception {
        ResultActions perform = mockMvc.perform(delete(PRODUCT_ID_PATH));

        verifyUnauthorizedResponse(perform, PRODUCT_ID_PATH);
    }

    @Test
    @DisplayName("상품 삭제 테스트-권한 부족")
    void deleteProductTest_NoPermission() throws Exception {
        ResultActions perform = mockMvc.perform(delete(PRODUCT_ID_PATH)
                .header(USER_ID_HEADER, 1L)
                .header(USER_ROLE_HEADER, USER_ROLE));

        verifyNoPermissionResponse(perform, PRODUCT_ID_PATH);
    }


    private ProductRequest createProductRequest(){
        return new ProductRequest(
                "name",
                "description",
                1L,
                createImageRequestList(),
                createProductOptionTypeRequestList(),
                createProductVariantRequestList()
        );
    }

    private UpdateProductBasicRequest createUpdateProductBasicRequest(){
        return new UpdateProductBasicRequest(
                "name",
                "description",
                1L
        );
    }

    private List<ImageRequest> createImageRequestList(){
        return List.of(new ImageRequest("http://test.jpg", 0));
    }

    private List<ProductOptionTypeRequest> createProductOptionTypeRequestList(){
        return List.of(new ProductOptionTypeRequest(1L, 0));
    }

    private List<ProductVariantRequest> createProductVariantRequestList(){
        return List.of(new ProductVariantRequest("sku",
                1, 1, 0,
                createProductOptionValueIdsRequest()));
    }

    private List<Long> createProductOptionValueIdsRequest(){
        return List.of(1L);
    }

}
