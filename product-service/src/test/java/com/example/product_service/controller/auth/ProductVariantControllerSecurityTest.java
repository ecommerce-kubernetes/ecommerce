package com.example.product_service.controller.auth;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.common.advice.CustomAccessDeniedHandler;
import com.example.product_service.common.advice.CustomAuthenticationEntryPoint;
import com.example.product_service.config.WebSecurity;
import com.example.product_service.controller.ProductVariantController;
import com.example.product_service.controller.util.UserRole;
import com.example.product_service.dto.request.variant.UpdateProductVariantRequest;
import com.example.product_service.service.ProductVariantService;
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

import static com.example.product_service.controller.util.ControllerTestHelper.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(ProductVariantController.class)
@Import({WebSecurity.class, CustomAccessDeniedHandler.class, CustomAuthenticationEntryPoint.class, MessageSourceUtil.class})
@AutoConfigureMockMvc
public class ProductVariantControllerSecurityTest {
    private static final String BASE_PATH = "/variants";
    private static final String PRODUCT_VARIANT_ID_PATH = BASE_PATH + "/1";
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    ProductVariantService productVariantService;

    @Test
    @DisplayName("상품 변형 수정 테스트-인증 에러")
    void updateProductVariantTest_UnAuthorized() throws Exception {
        ResultActions perform =
                performWithBody(mockMvc, patch(PRODUCT_VARIANT_ID_PATH), createUpdateProductVariantRequest());
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
        ResultActions perform = performWithBody(mockMvc, delete(PRODUCT_VARIANT_ID_PATH), null);
        verifyUnauthorizedResponse(perform, PRODUCT_VARIANT_ID_PATH);
    }

    @Test
    @DisplayName("상품 변형 삭제 테스트-권한 부족")
    void deleteProductVariantTest_NoPermission() throws Exception {
        ResultActions perform = performWithAuthAndBody(mockMvc, delete(PRODUCT_VARIANT_ID_PATH), null, UserRole.ROLE_USER);
        verifyNoPermissionResponse(perform, PRODUCT_VARIANT_ID_PATH);
    }

    private UpdateProductVariantRequest createUpdateProductVariantRequest(){
        return new UpdateProductVariantRequest(100, 10, 10, List.of(1L, 2L));
    }
}
