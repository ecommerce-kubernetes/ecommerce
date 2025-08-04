package com.example.product_service.controller.auth;

import com.example.product_service.common.advice.CustomAccessDeniedHandler;
import com.example.product_service.common.advice.CustomAuthenticationEntryPoint;
import com.example.product_service.config.WebSecurity;
import com.example.product_service.controller.ProductImageController;
import com.example.product_service.dto.request.image.AddImageRequest;
import com.example.product_service.service.ProductImageService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(ProductImageController.class)
@Import({WebSecurity.class, CustomAccessDeniedHandler.class, CustomAuthenticationEntryPoint.class})
@AutoConfigureMockMvc
public class ProductImageControllerSecurityTest {
    private static final String BASE_PATH = "/products/1/images";

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    ProductImageService productImageService;

    @Test
    @DisplayName("상품 이미지 추가 테스트-인증 에러")
    void addImageTest_UnAuthorized() throws Exception {
        String jsonBody = toJson(createAddImageRequest());

        ResultActions perform = mockMvc.perform(post(BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody));

        verifyUnauthorizedResponse(perform, BASE_PATH);
    }

    @Test
    @DisplayName("상품 이미지 추가 테스트-권한 부족")
    void addImageTest_NoPermission() throws Exception {
        String jsonBody = toJson(createAddImageRequest());

        ResultActions perform = mockMvc.perform(post(BASE_PATH)
                .header(USER_ID_HEADER, 1L)
                .header(USER_ROLE_HEADER, USER_ROLE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody));

        verifyNoPermissionResponse(perform, BASE_PATH);
    }

    private AddImageRequest createAddImageRequest(){
        return new AddImageRequest(List.of("http://test1.jpg", "http://test2.jpg"));
    }
}
