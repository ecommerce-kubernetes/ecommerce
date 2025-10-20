package com.example.product_service.controller;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.common.advice.ErrorResponseEntityFactory;
import com.example.product_service.config.TestConfig;
import com.example.product_service.exception.NoPermissionException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static com.example.product_service.common.MessagePath.*;
import static com.example.product_service.util.ControllerTestHelper.*;
import static com.example.product_service.util.TestMessageUtil.getMessage;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ErrorResponseEntityFactory.class, TestConfig.class})
public class ReviewControllerTest {

    private static final String BASE_PATH = "/reviews";
    private static final String ID_PATH = BASE_PATH + "/1";

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    MessageSourceUtil ms;
    @MockitoBean
    ReviewService service;

    @BeforeEach
    void setUpMessages() {
        when(ms.getMessage(NOT_FOUND)).thenReturn("NotFound");
        when(ms.getMessage(BAD_REQUEST)).thenReturn("BadRequest");
        when(ms.getMessage(BAD_REQUEST_VALIDATION)).thenReturn("Validation Error");
        when(ms.getMessage(CONFLICT)).thenReturn("Conflict");
        when(ms.getMessage(FORBIDDEN)).thenReturn("Forbidden");
    }

    @Test
    @DisplayName("상품 리뷰 삭제 테스트-성공")
    void deleteReviewTest_success() throws Exception {
        doNothing().when(service).deleteReviewById(anyLong(), anyLong());

        ResultActions perform = performWithBody(mockMvc, delete(ID_PATH).header("X-User-Id", 1), null);
        verifySuccessResponse(perform, status().isNoContent(), null);
    }

    @Test
    @DisplayName("상품 리뷰 삭제 테스트-실패(헤더 없음)")
    void deleteReviewTest_no_X_User_Id_Header() throws Exception {
        ResultActions perform = performWithBody(mockMvc, delete(ID_PATH), null);
        verifyErrorResponse(perform, status().isBadRequest(), getMessage(BAD_REQUEST),
                "Required request header 'X-User-Id' for method parameter type Long is not present",
                ID_PATH);
    }

    @Test
    @DisplayName("상품 리뷰 삭제 테스트-실패(없음)")
    void deleteReviewTest_notFound() throws Exception {
        doThrow(new NotFoundException(getMessage(REVIEW_NOT_FOUND)))
                .when(service).deleteReviewById(anyLong(), anyLong());
        ResultActions perform = performWithBody(mockMvc, delete(ID_PATH).header("X-User-Id", 1), null);
        verifyErrorResponse(perform, status().isNotFound(), getMessage(NOT_FOUND),
                getMessage(REVIEW_NOT_FOUND), ID_PATH);
    }

    @Test
    @DisplayName("상품 리뷰 삭제 테스트-실패(권한 없음)")
    void deleteReviewTest_forbidden() throws Exception {
        doThrow(new NoPermissionException(getMessage(REVIEW_FORBIDDEN_DELETE)))
                .when(service).deleteReviewById(anyLong(), anyLong());
        ResultActions perform = performWithBody(mockMvc, delete(ID_PATH).header("X-User-Id", 1), null);
        verifyErrorResponse(perform, status().isForbidden(), getMessage(FORBIDDEN),
                getMessage(REVIEW_FORBIDDEN_DELETE), ID_PATH);
    }
}
