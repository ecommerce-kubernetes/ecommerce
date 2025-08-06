package com.example.product_service.controller.auth;

import com.example.product_service.common.advice.CustomAccessDeniedHandler;
import com.example.product_service.common.advice.CustomAuthenticationEntryPoint;
import com.example.product_service.config.WebSecurity;
import com.example.product_service.controller.ProductImageController;
import com.example.product_service.controller.util.UserRole;
import com.example.product_service.dto.request.image.AddImageRequest;
import com.example.product_service.dto.request.image.ImageRequest;
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

import static com.example.product_service.controller.util.ControllerTestHelper.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(ProductImageController.class)
@Import({WebSecurity.class, CustomAccessDeniedHandler.class, CustomAuthenticationEntryPoint.class})
@AutoConfigureMockMvc
public class ProductImageControllerSecurityTest {
    private static final String BASE_PATH = "/products/1/images";
    private static final String PRODUCT_IMAGE_ID_PATH = BASE_PATH + "/1";

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    ProductImageService productImageService;

    @Test
    @DisplayName("상품 이미지 추가 테스트-인증 에러")
    void addImageTest_UnAuthorized() throws Exception {
        ResultActions perform =
                performWithBody(mockMvc, post(BASE_PATH), createAddImageRequest());
        verifyUnauthorizedResponse(perform, BASE_PATH);
    }

    @Test
    @DisplayName("상품 이미지 추가 테스트-권한 부족")
    void addImageTest_NoPermission() throws Exception {
        ResultActions perform =
                performWithAuthAndBody(mockMvc, post(BASE_PATH), createAddImageRequest(), UserRole.ROLE_USER);
        verifyNoPermissionResponse(perform, BASE_PATH);
    }

    @Test
    @DisplayName("상품 이미지 수정 테스트-인증 에러")
    void updateImageTest_UnAuthorized() throws Exception {
        ResultActions perform =
                performWithBody(mockMvc, patch(PRODUCT_IMAGE_ID_PATH), createImageRequest());
        verifyUnauthorizedResponse(perform, PRODUCT_IMAGE_ID_PATH);
    }

    @Test
    @DisplayName("상품 이미지 수정 테스트-권한 부족")
    void updateImageTest_NoPermission() throws Exception {
        ResultActions perform =
                performWithAuthAndBody(mockMvc, patch(PRODUCT_IMAGE_ID_PATH), createImageRequest(), UserRole.ROLE_USER);
        verifyNoPermissionResponse(perform, PRODUCT_IMAGE_ID_PATH);
    }

    @Test
    @DisplayName("상품 이미지 삭제 테스트-인증 에러")
    void deleteImageTest_UnAuthorized() throws Exception {
        ResultActions perform = performWithBody(mockMvc, delete(PRODUCT_IMAGE_ID_PATH), null);
        verifyUnauthorizedResponse(perform, PRODUCT_IMAGE_ID_PATH);
    }

    @Test
    @DisplayName("상품 이미지 삭제 테스트-권한 부족")
    void deleteImageTest_NoPermission() throws Exception {
        ResultActions perform = performWithAuthAndBody(mockMvc, delete(PRODUCT_IMAGE_ID_PATH), null, UserRole.ROLE_USER);
        verifyNoPermissionResponse(perform, PRODUCT_IMAGE_ID_PATH);
    }

    private AddImageRequest createAddImageRequest(){
        return new AddImageRequest(List.of("http://test1.jpg", "http://test2.jpg"));
    }

    private ImageRequest createImageRequest(){
        return new ImageRequest("http://test.jpg", 1);
    }
}
