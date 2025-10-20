package com.example.product_service.controller;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.common.advice.ErrorResponseEntityFactory;
import com.example.product_service.config.TestConfig;
import com.example.product_service.dto.request.options.OptionValueRequest;
import com.example.product_service.dto.response.options.OptionValueResponse;
import com.example.product_service.exception.DuplicateResourceException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.service.OptionValueService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OptionValueController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ErrorResponseEntityFactory.class, TestConfig.class})
class OptionValueControllerTest {

    private static final String CREATE_OPTION_VALUE_PATH = "/option-types/1/option-values";
    private static final String BASE_PATH = "/option-values";
    private static final String ID_PATH = BASE_PATH + "/1";

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    MessageSourceUtil ms;
    @MockitoBean
    OptionValueService service;

    @BeforeEach
    void setUpMessages() {
        when(ms.getMessage(NOT_FOUND)).thenReturn("NotFound");
        when(ms.getMessage(BAD_REQUEST)).thenReturn("BadRequest");
        when(ms.getMessage(BAD_REQUEST_VALIDATION)).thenReturn("Validation Error");
        when(ms.getMessage(CONFLICT)).thenReturn("Conflict");
    }

    @Test
    @DisplayName("옵션 값 조회 테스트-성공")
    void getOptionValueTest_success() throws Exception {
        OptionValueResponse response = new OptionValueResponse(1L, 1L, "value1");
        when(service.getOptionValueById(anyLong())).thenReturn(response);
        ResultActions perform = performWithBody(mockMvc, get(ID_PATH), null);

        verifySuccessResponse(perform, status().isOk(), response);
    }

    @Test
    @DisplayName("옵션 값 조회 테스트-실패(없음)")
    void getOptionValueTest_notFound() throws Exception {
        when(service.getOptionValueById(anyLong()))
                .thenThrow(new NotFoundException(getMessage(OPTION_VALUE_NOT_FOUND)));
        ResultActions perform = performWithBody(mockMvc, get(ID_PATH), null);
        verifyErrorResponse(perform, status().isNotFound(), getMessage(NOT_FOUND),
                getMessage(OPTION_VALUE_NOT_FOUND), ID_PATH);
    }

    @Test
    @DisplayName("옵션 값 수정 테스트-성공")
    void updateOptionValueTest_success() throws Exception {
        OptionValueRequest request = new OptionValueRequest("updatedValue");
        OptionValueResponse response = new OptionValueResponse(1L, 1L, "updatedValue");
        when(service.updateOptionValueById(anyLong(), any(OptionValueRequest.class))).thenReturn(response);

        ResultActions perform = performWithBody(mockMvc, patch(ID_PATH), request);
        verifySuccessResponse(perform, status().isOk(), response);
    }

    @Test
    @DisplayName("옵션 값 수정 테스트-실패(검증)")
    void updateOptionValueTest_validation() throws Exception {
        OptionValueRequest request = new OptionValueRequest("");
        ResultActions perform = performWithBody(mockMvc, patch(ID_PATH), request);
        verifyErrorResponse(perform, status().isBadRequest(), getMessage(BAD_REQUEST), getMessage(BAD_REQUEST_VALIDATION), ID_PATH);
    }

    @Test
    @DisplayName("옵션 값 수정 테스트-실패(없음)")
    void updateOptionValueTest_notFound() throws Exception {
        OptionValueRequest request = new OptionValueRequest("updatedOptionValue");
        when(service.updateOptionValueById(anyLong(), any(OptionValueRequest.class)))
                .thenThrow(new NotFoundException(getMessage(OPTION_VALUE_NOT_FOUND)));

        ResultActions perform = performWithBody(mockMvc, patch(ID_PATH), request);
        verifyErrorResponse(perform, status().isNotFound(), getMessage(NOT_FOUND), getMessage(OPTION_VALUE_NOT_FOUND), ID_PATH);
    }

    @Test
    @DisplayName("옵션 값 수정 테스트-실패(중복)")
    void updateOptionValueTest_conflict() throws Exception {
        OptionValueRequest request = new OptionValueRequest("duplicatedName");
        when(service.updateOptionValueById(anyLong(), any(OptionValueRequest.class)))
                .thenThrow(new DuplicateResourceException(getMessage(OPTION_VALUE_CONFLICT)));

        ResultActions perform = performWithBody(mockMvc, patch(ID_PATH), request);
        verifyErrorResponse(perform, status().isConflict(), getMessage(CONFLICT), getMessage(OPTION_VALUE_CONFLICT), ID_PATH);
    }

    @Test
    @DisplayName("옵션 값 삭제 테스트-성공")
    void deleteOptionValueTest_success() throws Exception {
        doNothing().when(service).deleteOptionValueById(anyLong());

        ResultActions perform = performWithBody(mockMvc, delete(ID_PATH), null);
        verifySuccessResponse(perform, status().isNoContent(), null);
    }

    @Test
    @DisplayName("옵션 값 삭제 테스트-실패(없음)")
    void deleteOptionValueTest_notFound() throws Exception {
        doThrow(new NotFoundException(getMessage(OPTION_VALUE_NOT_FOUND)))
                .when(service).deleteOptionValueById(any());

        ResultActions perform = performWithBody(mockMvc, delete(ID_PATH), null);
        verifyErrorResponse(perform, status().isNotFound(), getMessage(NOT_FOUND),
                getMessage(OPTION_VALUE_NOT_FOUND), ID_PATH);
    }
}