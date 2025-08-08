package com.example.product_service.controller;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.controller.util.ControllerTestHelper;
import com.example.product_service.controller.util.TestMessageUtil;
import com.example.product_service.dto.request.image.AddImageRequest;
import com.example.product_service.dto.response.image.ImageResponse;
import com.example.product_service.service.ProductImageService;
import com.example.product_service.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static com.example.product_service.controller.util.ControllerTestHelper.*;
import static com.example.product_service.controller.util.TestMessageUtil.getMessage;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductImageController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductImageControllerTest {

    private static final String CREATE_PATH = "/products/1/images";
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    MessageSourceUtil ms;
    @MockitoBean
    ProductImageService service;

    @BeforeEach
    void setUpMessages() {
        when(ms.getMessage("badRequest")).thenReturn("BadRequest");
        when(ms.getMessage("badRequest.validation")).thenReturn("Validation Error");
        when(ms.getMessage("conflict")).thenReturn("Conflict");
    }

    @Test
    @DisplayName("상품 이미지 추가 테스트-성공")
    void addImagesTest_success() throws Exception {
        AddImageRequest request = new AddImageRequest(List.of("http://test1.jpg", "http://test2.jpg"));
        List<ImageResponse> response = List.of(new ImageResponse(1L, "http://test1.jpg", 2),
                new ImageResponse(1L, "http://test2.jpg", 3));
        when(service.addImages(anyLong(), any(AddImageRequest.class)))
                .thenReturn(response);
        ResultActions perform = performWithBody(mockMvc, post(CREATE_PATH), request);
        verifySuccessResponse(perform, status().isCreated(), response);
    }

    @Test
    @DisplayName("상품 이미지 추가 테스트-실패(검증)")
    void addImagesTest_validation() throws Exception {
        AddImageRequest request = new AddImageRequest(List.of());
        ResultActions perform = performWithBody(mockMvc, post(CREATE_PATH), request);
        verifyErrorResponse(perform, status().isBadRequest(), getMessage("badRequest"),
                getMessage("badRequest.validation"), CREATE_PATH);
    }

}