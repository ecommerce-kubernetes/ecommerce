package com.example.product_service.controller;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.request.image.AddImageRequest;
import com.example.product_service.dto.request.image.ImageRequest;
import com.example.product_service.dto.response.image.ImageResponse;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.service.ProductImageService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductImageController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductImageControllerTest {

    private static final String CREATE_PATH = "/products/1/images";
    private static final String BASE_PATH = "/product-images";
    private static final String ID_PATH = BASE_PATH + "/1";
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

    @Test
    @DisplayName("상품 이미지 추가 테스트-실패(상품 없음)")
    void addImagesTest_notFound() throws Exception {
        AddImageRequest request = new AddImageRequest(List.of("http://test1.jpg", "http://test2.jpg"));
        when(service.addImages(anyLong(),any(AddImageRequest.class)))
                .thenThrow(new NotFoundException(getMessage("product.notFound")));
        ResultActions perform = performWithBody(mockMvc, post(CREATE_PATH), request);
        verifyErrorResponse(perform, status().isNotFound(), getMessage("notFound"),
                getMessage("product.notFound"), CREATE_PATH);
    }

    @Test
    @DisplayName("상품 이미지 수정 테스트-성공")
    void updateImageTest_success() throws Exception {
        ImageRequest request = new ImageRequest("http://test.jpg", 0);
        ImageResponse response = new ImageResponse(1L, "http://test1.jpg", 0);
        when(service.updateImageById(anyLong(), any(ImageRequest.class)))
                .thenReturn(response);
        ResultActions perform = performWithBody(mockMvc, patch(ID_PATH), request);
        verifySuccessResponse(perform, status().isOk(), response);
    }

    @Test
    @DisplayName("상품 이미지 수정 테스트-실패(검증)")
    void updateImageTest_validation() throws Exception {
        ImageRequest request = new ImageRequest();
        ResultActions perform = performWithBody(mockMvc, patch(ID_PATH), request);
        verifyErrorResponse(perform, status().isBadRequest(), getMessage("badRequest"),
                getMessage("badRequest.validation"), ID_PATH);
    }

    @Test
    @DisplayName("상품 이미지 수정 테스트-실패(없음)")
    void updateImageTest_notFound() throws Exception {
        ImageRequest request = new ImageRequest("http://test.jpg", 0);
        when(service.updateImageById(anyLong(), any(ImageRequest.class)))
                .thenThrow(new NotFoundException(getMessage("product-image.notFound")));

        ResultActions perform = performWithBody(mockMvc, patch(ID_PATH), request);
        verifyErrorResponse(perform, status().isNotFound(), getMessage("notFound"),
                getMessage("product-image.notFound"), ID_PATH);
    }

    @Test
    @DisplayName("상품 이미지 삭제 테스트-성공")
    void deleteImageTest_success() throws Exception {
        doNothing().when(service).deleteImageById(anyLong());
        ResultActions perform = performWithBody(mockMvc, delete(ID_PATH), null);
        verifySuccessResponse(perform, status().isNoContent(), null);
    }

    @Test
    @DisplayName("상품 이미지 삭제 테스트-실패(없음)")
    void deleteImageTest_notFound() throws Exception {
        doThrow(new NotFoundException(getMessage("product-image.notFound")))
                .when(service).deleteImageById(anyLong());
        ResultActions perform = performWithBody(mockMvc, delete(ID_PATH), null);
        verifyErrorResponse(perform, status().isNotFound(), getMessage("notFound"),
                getMessage("product-image.notFound"), ID_PATH);
    }

}