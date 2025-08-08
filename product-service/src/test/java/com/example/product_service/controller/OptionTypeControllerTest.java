package com.example.product_service.controller;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.request.options.OptionTypeRequest;
import com.example.product_service.dto.request.options.OptionValueRequest;
import com.example.product_service.dto.response.options.OptionTypeResponse;
import com.example.product_service.dto.response.options.OptionValueResponse;
import com.example.product_service.exception.DuplicateResourceException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.service.OptionTypeService;
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
import static com.example.product_service.controller.util.MessagePath.*;
import static com.example.product_service.controller.util.TestMessageUtil.getMessage;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OptionTypeController.class)
@AutoConfigureMockMvc(addFilters = false)
class OptionTypeControllerTest {

    private static final String BASE_PATH = "/option-types";
    private static final String VALUES_PATH = BASE_PATH + "/1/option-values";
    private static final String ID_PATH = BASE_PATH + "/1";

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    MessageSourceUtil ms;
    @MockitoBean
    OptionTypeService service;

    @BeforeEach
    void setUpMessages() {
        when(ms.getMessage(BAD_REQUEST)).thenReturn("BadRequest");
        when(ms.getMessage(BAD_REQUEST_VALIDATION)).thenReturn("Validation Error");
        when(ms.getMessage(CONFLICT)).thenReturn("Conflict");
    }

    @Test
    @DisplayName("옵션 타입 생성 테스트-성공")
    void createOptionTypeTest_success() throws Exception {
        String optionTypeName = "newOptionType";
        OptionTypeRequest request = new OptionTypeRequest(optionTypeName);
        OptionTypeResponse response = new OptionTypeResponse(1L, optionTypeName);

        when(service.saveOptionType(any(OptionTypeRequest.class))).thenReturn(response);

        ResultActions perform = performWithBody(mockMvc, post(BASE_PATH), request);
        verifySuccessResponse(perform, status().isCreated(), response);
    }

    @Test
    @DisplayName("옵션 타입 생성 테스트-실패(검증)")
    void createOptionTypeTest_validation() throws Exception {
        OptionTypeRequest request = new OptionTypeRequest();
        ResultActions perform = performWithBody(mockMvc, post(BASE_PATH), request);
        verifyErrorResponse(perform, status().isBadRequest(),
                getMessage(BAD_REQUEST), getMessage(BAD_REQUEST_VALIDATION), BASE_PATH);

        perform.andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    @DisplayName("옵션타입 생성 테스트-실패(중복)")
    void createOptionTypeTest_conflict() throws Exception {
        String duplicateName = "duplicateName";
        OptionTypeRequest request = new OptionTypeRequest(duplicateName);
        when(service.saveOptionType(any(OptionTypeRequest.class)))
                .thenThrow(new DuplicateResourceException(getMessage(OPTION_TYPE_CONFLICT)));

        ResultActions perform = performWithBody(mockMvc, post(BASE_PATH), request);
        verifyErrorResponse(perform, status().isConflict(), getMessage(CONFLICT)
                ,getMessage(OPTION_TYPE_CONFLICT),BASE_PATH);

    }

    @Test
    @DisplayName("옵션 값 저장 테스트-성공")
    void createOptionValueTest_success() throws Exception {
        OptionValueRequest request = new OptionValueRequest("value");
        OptionValueResponse response = new OptionValueResponse(1L, 1L, "value");
        when(service.saveOptionValue(anyLong(), any(OptionValueRequest.class))).thenReturn(response);

        ResultActions perform = performWithBody(mockMvc, post(VALUES_PATH), request);
        verifySuccessResponse(perform, status().isCreated(), response);
    }

    @Test
    @DisplayName("옵션 값 저장 테스트-실패(검증)")
    void createOptionValueTest_validation() throws Exception {
        OptionValueRequest request = new OptionValueRequest("");

        ResultActions perform = performWithBody(mockMvc, post(VALUES_PATH), request);
        verifyErrorResponse(perform, status().isBadRequest(), getMessage(BAD_REQUEST),
                getMessage(BAD_REQUEST_VALIDATION), VALUES_PATH);

        perform.andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    @DisplayName("옵션 값 저장 테스트-실패(중복)")
    void createOptionValueTest_conflict() throws Exception {
        OptionValueRequest request = new OptionValueRequest("duplicated");
        when(service.saveOptionValue(anyLong(), any(OptionValueRequest.class)))
                .thenThrow(new DuplicateResourceException(getMessage(OPTION_VALUE_CONFLICT)));

        ResultActions perform = performWithBody(mockMvc, post(VALUES_PATH), request);
        verifyErrorResponse(perform, status().isConflict(), getMessage(CONFLICT),
                getMessage(OPTION_VALUE_CONFLICT), VALUES_PATH);
    }

    @Test
    @DisplayName("옵션 값 저장 테스트-실패(옵션 타입 없음)")
    void createOptionValue_notFound() throws Exception {
        OptionValueRequest request = new OptionValueRequest("values");

        when(service.saveOptionValue(anyLong(), any(OptionValueRequest.class)))
                .thenThrow(new NotFoundException(getMessage(OPTION_TYPE_NOT_FOUND)));
        ResultActions perform = performWithBody(mockMvc, post(VALUES_PATH), request);

        verifyErrorResponse(perform, status().isNotFound(), getMessage(NOT_FOUND),
                getMessage(OPTION_TYPE_NOT_FOUND), VALUES_PATH);
    }

    @Test
    @DisplayName("옵션 타입 조회 테스트-성공")
    void getOptionTypesTest_success() throws Exception {
        List<OptionTypeResponse> optionTypes = List.of(new OptionTypeResponse(1L, "optionTypes"));

        when(service.getOptionTypes()).thenReturn(optionTypes);

        ResultActions perform = performWithBody(mockMvc, get(BASE_PATH), null);
        verifySuccessResponse(perform, status().isOk(), optionTypes);
    }


    @Test
    @DisplayName("옵션 타입 값 조회 테스트-성공")
    void getValuesByTypeTest_success() throws Exception {
        List<OptionValueResponse> response = List.of(new OptionValueResponse(1L, 1L, "value1"),
                new OptionValueResponse(2L, 1L, "value2"));

        when(service.getOptionValuesByTypeId(anyLong())).thenReturn(response);

        ResultActions perform = performWithBody(mockMvc, get(VALUES_PATH), null);
        verifySuccessResponse(perform, status().isOk(), response);
    }

    @Test
    @DisplayName("옵션 타입 값 조회 테스트-실패(없음)")
    void getValuesByTypeTest_notFound() throws Exception {
        when(service.getOptionValuesByTypeId(anyLong()))
                .thenThrow(new NotFoundException(getMessage(OPTION_TYPE_NOT_FOUND)));

        ResultActions perform = performWithBody(mockMvc, get(VALUES_PATH), null);
        verifyErrorResponse(perform, status().isNotFound(),
                getMessage(NOT_FOUND), getMessage(OPTION_TYPE_NOT_FOUND), VALUES_PATH);
    }


    @Test
    @DisplayName("옵션 타입 수정 테스트-성공")
    void updateOptionTypeTest_success() throws Exception {
        String updateName = "updatedOptionType";

        OptionTypeRequest request = new OptionTypeRequest(updateName);
        OptionTypeResponse response = new OptionTypeResponse(1L, updateName);

        when(service.updateOptionTypeById(anyLong(), any(OptionTypeRequest.class))).thenReturn(response);

        ResultActions perform = performWithBody(mockMvc, patch(ID_PATH), request);
        verifySuccessResponse(perform, status().isOk(), response);
    }

    @Test
    @DisplayName("옵션 타입 수정 테스트-실패(검증)")
    void updateOptionTypeTest_validation() throws Exception {
        OptionTypeRequest request = new OptionTypeRequest("");

        ResultActions perform = performWithBody(mockMvc, patch(ID_PATH), request);
        verifyErrorResponse(perform, status().isBadRequest(), getMessage(BAD_REQUEST),
                getMessage(BAD_REQUEST_VALIDATION), ID_PATH);

        perform.andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    @DisplayName("옵션 타입 수정 테스트-실패(없음)")
    void updateOptionTypeTest_notFound() throws Exception {
        OptionTypeRequest request = new OptionTypeRequest("updatedOptionType");

        when(service.updateOptionTypeById(anyLong(), any(OptionTypeRequest.class)))
                .thenThrow(new NotFoundException(getMessage(OPTION_TYPE_NOT_FOUND)));

        ResultActions perform = performWithBody(mockMvc, patch(ID_PATH), request);
        verifyErrorResponse(perform, status().isNotFound(), getMessage(NOT_FOUND),
                getMessage(OPTION_TYPE_NOT_FOUND), ID_PATH);
    }

    @Test
    @DisplayName("옵션 타입 삭제 테스트-성공")
    void deleteOptionTypeTest_success() throws Exception {

        doNothing().when(service).deleteOptionTypeById(anyLong());

        ResultActions perform = performWithBody(mockMvc, delete(ID_PATH), null);
        verifySuccessResponse(perform, status().isNoContent(), null);
    }

    @Test
    @DisplayName("옵션 타입 삭제 테스트-실패(없음)")
    void deleteOptionTypeTest_notFound() throws Exception {
        doThrow(new NotFoundException(getMessage(OPTION_TYPE_NOT_FOUND)))
                .when(service).deleteOptionTypeById(anyLong());

        ResultActions perform = performWithBody(mockMvc, delete(ID_PATH), null);
        verifyErrorResponse(perform, status().isNotFound(), getMessage(NOT_FOUND),
                getMessage(OPTION_TYPE_NOT_FOUND), ID_PATH);
    }
}