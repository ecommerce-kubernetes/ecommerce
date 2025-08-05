package com.example.product_service.controller.auth;

import com.example.product_service.common.advice.CustomAccessDeniedHandler;
import com.example.product_service.common.advice.CustomAuthenticationEntryPoint;
import com.example.product_service.config.WebSecurity;
import com.example.product_service.controller.ProductVariantController;
import com.example.product_service.controller.util.UserRole;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
import com.example.product_service.dto.request.variant.UpdateProductVariantRequest;
import com.example.product_service.service.ProductImageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static com.example.product_service.controller.util.SecurityTestHelper.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(ProductVariantController.class)
@Import({WebSecurity.class, CustomAccessDeniedHandler.class, CustomAuthenticationEntryPoint.class})
@AutoConfigureMockMvc
public class ProductVariantControllerSecurityTest {
    private static final String BASE_PATH = "/products/1/variants";
    private static final String PRODUCT_VARIANT_ID_PATH = BASE_PATH + "/1";
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    ProductImageService productImageService;

    @Test
    @DisplayName("상품 변형 추가 테스트-인증 에러")
    void addVariantTest_UnAuthorized() throws Exception {
        ResultActions perform = performWithAuthAndBody(mockMvc, post(BASE_PATH), createProductVariantRequest(), null);
        verifyUnauthorizedResponse(perform, BASE_PATH);
    }

    @Test
    @DisplayName("상품 변형 추가 테스트-권한 부족")
    void addVariantTest_NoPermission() throws Exception {
        ResultActions perform =
                performWithAuthAndBody(mockMvc, post(BASE_PATH), createProductVariantRequest(), UserRole.ROLE_USER);
        verifyNoPermissionResponse(perform, BASE_PATH);
    }

    @Test
    @DisplayName("상품 변형 수정 테스트-인증 에러")
    void updateProductVariantTest_UnAuthorized() throws Exception {
        ResultActions perform =
                performWithAuthAndBody(mockMvc, patch(PRODUCT_VARIANT_ID_PATH), createUpdateProductVariantRequest(), null);
        verifyUnauthorizedResponse(perform, PRODUCT_VARIANT_ID_PATH);
    }

    @Test
    @DisplayName("상품 변형 수정 테스트-권한 부족")
    void updateProductVariantTest_NoPermission() throws Exception {
        ResultActions perform =
                performWithAuthAndBody(mockMvc, patch(PRODUCT_VARIANT_ID_PATH), createUpdateProductVariantRequest(), UserRole.ROLE_USER);
        verifyNoPermissionResponse(perform, PRODUCT_VARIANT_ID_PATH);
    }

    @Test
    @DisplayName("상품 변형 삭제 테스트-인증 에러")
    void deleteProductVariantTest_UnAuthorized() throws Exception {
        ResultActions perform = performWithAuthAndBody(mockMvc, delete(PRODUCT_VARIANT_ID_PATH), null, null);
        verifyUnauthorizedResponse(perform, PRODUCT_VARIANT_ID_PATH);
    }

    @Test
    @DisplayName("상품 변형 삭제 테스트-권한 부족")
    void deleteProductVariantTest_NoPermission() throws Exception {
        ResultActions perform = performWithAuthAndBody(mockMvc, delete(PRODUCT_VARIANT_ID_PATH), null, UserRole.ROLE_USER);
        verifyNoPermissionResponse(perform, PRODUCT_VARIANT_ID_PATH);
    }

    private ProductVariantRequest createProductVariantRequest(){
        return new ProductVariantRequest("sku", 100, 100, 10, List.of(1L,2L));
    }

    private UpdateProductVariantRequest createUpdateProductVariantRequest(){
        return new UpdateProductVariantRequest(100, 10, 10, List.of(1L, 2L));
    }
}
