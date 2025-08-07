package com.example.product_service.controller;

import com.example.product_service.common.MessageSourceUtil;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static com.example.product_service.controller.util.ControllerTestHelper.*;
import static com.example.product_service.controller.util.TestMessageUtil.getMessage;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OptionValueController.class)
@AutoConfigureMockMvc(addFilters = false)
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
        when(ms.getMessage("badRequest")).thenReturn("BadRequest");
        when(ms.getMessage("badRequest.validation")).thenReturn("Validation Error");
        when(ms.getMessage("conflict")).thenReturn("Conflict");
    }

    @Test
    @DisplayName("옵션 값 저장 테스트-성공")
    void createOptionValueTest_success() throws Exception {
        OptionValueRequest request = new OptionValueRequest("value");
        OptionValueResponse response = new OptionValueResponse(1L, 1L, "value");
        when(service.saveOptionValue(anyLong(), any(OptionValueRequest.class))).thenReturn(response);

        ResultActions perform = performWithBody(mockMvc, post(CREATE_OPTION_VALUE_PATH), request);
        verifySuccessResponse(perform, status().isCreated(), response);
    }

    @Test
    @DisplayName("옵션 값 저장 테스트-실패(검증)")
    void createOptionValueTest_validation() throws Exception {
        OptionValueRequest request = new OptionValueRequest("");

        ResultActions perform = performWithBody(mockMvc, post(CREATE_OPTION_VALUE_PATH), request);
        verifyErrorResponse(perform, status().isBadRequest(), getMessage("badRequest"),
                getMessage("badRequest.validation"), CREATE_OPTION_VALUE_PATH );

        perform.andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    @DisplayName("옵션 값 저장 테스트-실패(중복)")
    void createOptionValueTest_conflict() throws Exception {
        OptionValueRequest request = new OptionValueRequest("duplicated");
        when(service.saveOptionValue(anyLong(), any(OptionValueRequest.class)))
                .thenThrow(new DuplicateResourceException(getMessage("option-value.conflict")));

        ResultActions perform = performWithBody(mockMvc, post(CREATE_OPTION_VALUE_PATH), request);
        verifyErrorResponse(perform, status().isConflict(), getMessage("conflict"),
                getMessage("option-value.conflict"), CREATE_OPTION_VALUE_PATH);
    }

    @Test
    @DisplayName("옵션 값 저장 테스트-실패(옵션 타입 없음)")
    void createOptionValue_notFound() throws Exception {
        OptionValueRequest request = new OptionValueRequest("values");

        when(service.saveOptionValue(anyLong(), any(OptionValueRequest.class)))
                .thenThrow(new NotFoundException(getMessage("option-type.notFound")));
        ResultActions perform = performWithBody(mockMvc, post(CREATE_OPTION_VALUE_PATH), request);

        verifyErrorResponse(perform, status().isNotFound(), getMessage("notFound"),
                getMessage("option-type.notFound"), CREATE_OPTION_VALUE_PATH);
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
                .thenThrow(new NotFoundException(getMessage("option-value.notFound")));
        ResultActions perform = performWithBody(mockMvc, get(ID_PATH), null);
        verifyErrorResponse(perform, status().isNotFound(), getMessage("notFound"),
                getMessage("option-value.notFound"), ID_PATH);
    }

    @Test
    @DisplayName("옵션 값 수정 테스트-성공")
    void updateOptionValueTest_success() throws Exception {
        OptionValueRequest request = new OptionValueRequest("updatedValue");
        OptionValueResponse response = new OptionValueResponse(1L, 1L, "updatedValue");
        when(service.updateOptionValue(anyLong(), any(OptionValueRequest.class))).thenReturn(response);

        ResultActions perform = performWithBody(mockMvc, patch(ID_PATH), request);
        verifySuccessResponse(perform, status().isOk(), response);
    }

    @Test
    @DisplayName("옵션 값 수정 테스트-실패(검증)")
    void updateOptionValueTest_validation() throws Exception {
        OptionValueRequest request = new OptionValueRequest("");
        ResultActions perform = performWithBody(mockMvc, patch(ID_PATH), request);
        verifyErrorResponse(perform, status().isBadRequest(), getMessage("badRequest"), getMessage("badRequest.validation"), ID_PATH);
    }

    @Test
    @DisplayName("옵션 값 수정 테스트-실패(없음)")
    void updateOptionValueTest_notFound() throws Exception {
        OptionValueRequest request = new OptionValueRequest("updatedOptionValue");
        when(service.updateOptionValue(anyLong(), any(OptionValueRequest.class)))
                .thenThrow(new NotFoundException(getMessage("option-value.notFound")));

        ResultActions perform = performWithBody(mockMvc, patch(ID_PATH), request);
        verifyErrorResponse(perform, status().isNotFound(), getMessage("notFound"), getMessage("option-value.notFound"), ID_PATH);
    }

    @Test
    @DisplayName("옵션 값 수정 테스트-실패(중복)")
    void updateOptionValueTest_conflict() throws Exception {
        OptionValueRequest request = new OptionValueRequest("duplicatedName");
        when(service.updateOptionValue(anyLong(), any(OptionValueRequest.class)))
                .thenThrow(new DuplicateResourceException(getMessage("option-value.conflict")));

        ResultActions perform = performWithBody(mockMvc, patch(ID_PATH), request);
        verifyErrorResponse(perform, status().isConflict(), getMessage("conflict"), getMessage("option-value.conflict"), ID_PATH);
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
        doThrow(new NotFoundException(getMessage("option-value.notFound")))
                .when(service).deleteOptionValueById(any());

        ResultActions perform = performWithBody(mockMvc, delete(ID_PATH), null);
        verifyErrorResponse(perform, status().isNotFound(), getMessage("notFound"),
                getMessage("option-value.notFound"), ID_PATH);
    }
}