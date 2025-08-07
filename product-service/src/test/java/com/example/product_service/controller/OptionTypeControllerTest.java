package com.example.product_service.controller;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.request.options.OptionTypeRequest;
import com.example.product_service.dto.response.options.OptionTypeResponse;
import com.example.product_service.dto.response.options.OptionValuesResponse;
import com.example.product_service.exception.DuplicateResourceException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.service.OptionTypeService;
import lombok.extern.slf4j.Slf4j;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OptionTypeController.class)
@Slf4j
@AutoConfigureMockMvc(addFilters = false)
class OptionTypeControllerTest {

    private static final String BASE_PATH = "/option-types";

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    MessageSourceUtil ms;
    @MockitoBean
    OptionTypeService service;

    @BeforeEach
    void setUpMessages() {
        when(ms.getMessage("badRequest")).thenReturn("BadRequest");
        when(ms.getMessage("badRequest.validation")).thenReturn("Validation Error");
        when(ms.getMessage("conflict")).thenReturn("Conflict");
    }

    @Test
    @DisplayName("옵션 타입 생성 테스트-성공")
    void createOptionTypeTest_success() throws Exception {
        String optionTypeName = "newOptionType";
        OptionTypeRequest request = new OptionTypeRequest(optionTypeName);
        OptionTypeResponse response = new OptionTypeResponse(1L, optionTypeName);

        when(service.saveOptionTypes(any(OptionTypeRequest.class))).thenReturn(response);

        ResultActions perform = performWithBody(mockMvc, post(BASE_PATH), request);
        verifySuccessResponse(perform, status().isCreated(), response);
    }

    @Test
    @DisplayName("옵션 타입 생성 테스트-실패(검증)")
    void createOptionTypeTest_validation() throws Exception {
        OptionTypeRequest request = new OptionTypeRequest();
        ResultActions perform = performWithBody(mockMvc, post(BASE_PATH), request);
        verifyErrorResponse(perform, status().isBadRequest(),
                getMessage("badRequest"), getMessage("badRequest.validation"), BASE_PATH);

        perform.andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    @DisplayName("옵션타입 생성 테스트-실패(중복)")
    void createOptionTypeTest_conflict() throws Exception {
        String duplicateName = "duplicateName";
        OptionTypeRequest request = new OptionTypeRequest(duplicateName);
        when(service.saveOptionTypes(any(OptionTypeRequest.class)))
                .thenThrow(new DuplicateResourceException(getMessage("option-type.conflict")));

        ResultActions perform = performWithBody(mockMvc, post(BASE_PATH), request);
        verifyErrorResponse(perform, status().isConflict(), getMessage("conflict")
                ,getMessage("option-type.conflict"),BASE_PATH);

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
        List<OptionValuesResponse> response = List.of(new OptionValuesResponse(1L, "value1", 1L),
                new OptionValuesResponse(2L, "value2", 1L));

        when(service.getOptionValuesByTypeId(anyLong())).thenReturn(response);

        ResultActions perform = performWithBody(mockMvc, get(BASE_PATH + "/1/values"), null);
        verifySuccessResponse(perform, status().isOk(), response);
    }

    @Test
    @DisplayName("옵션 타입 값 조회 테스트-실패(없음)")
    void getValuesByTypeTest_notFound() throws Exception {
        when(service.getOptionValuesByTypeId(anyLong()))
                .thenThrow(new NotFoundException(getMessage("notFound.message")));

        ResultActions perform = performWithBody(mockMvc, get(BASE_PATH + "/1/values"), null);
        verifyErrorResponse(perform, status().isNotFound(),
                getMessage("notFound"), getMessage("notFound.message"), BASE_PATH + "/1/values");
    }


    @Test
    @DisplayName("옵션 타입 수정 테스트-성공")
    void updateOptionTypeTest_success() throws Exception {
        String updateName = "updatedOptionType";

        OptionTypeRequest request = new OptionTypeRequest(updateName);
        OptionTypeResponse response = new OptionTypeResponse(1L, updateName);

        when(service.updateOptionTypeById(anyLong(), any(OptionTypeRequest.class))).thenReturn(response);

        ResultActions perform = performWithBody(mockMvc, patch(BASE_PATH + "/1"), request);
        verifySuccessResponse(perform, status().isOk(), response);
    }

    @Test
    @DisplayName("옵션 타입 수정 테스트-실패(검증)")
    void updateOptionTypeTest_validation() throws Exception {
        OptionTypeRequest request = new OptionTypeRequest("");

        ResultActions perform = performWithBody(mockMvc, patch(BASE_PATH + "/1"), request);
        verifyErrorResponse(perform, status().isBadRequest(), getMessage("badRequest"),
                getMessage("badRequest.validation"), BASE_PATH + "/1");
    }

    @Test
    @DisplayName("옵션 타입 수정 테스트-실패(없음)")
    void updateOptionTypeTest_notFound() throws Exception {
        OptionTypeRequest request = new OptionTypeRequest("updatedOptionType");

        when(service.updateOptionTypeById(anyLong(), any(OptionTypeRequest.class)))
                .thenThrow(new NotFoundException(getMessage("notFound.message")));

        ResultActions perform = performWithBody(mockMvc, patch(BASE_PATH + "/1"), request);
        verifyErrorResponse(perform, status().isNotFound(), getMessage("notFound"),
                getMessage("notFound.message"), BASE_PATH + "/1");
    }

    @Test
    @DisplayName("옵션 타입 삭제 테스트-성공")
    void deleteOptionTypeTest_success() throws Exception {

        doNothing().when(service).deleteOptionTypeById(anyLong());

        ResultActions perform = performWithBody(mockMvc, delete(BASE_PATH + "/1"), null);
        verifySuccessResponse(perform, status().isNoContent(), null);
    }

    @Test
    @DisplayName("옵션 타입 삭제 테스트-실패(없음)")
    void deleteOptionTypeTest_notFound() throws Exception {
        doThrow(new NotFoundException(getMessage("notFound.message")))
                .when(service).deleteOptionTypeById(anyLong());

        ResultActions perform = performWithBody(mockMvc, delete(BASE_PATH + "/1"), null);
        verifyErrorResponse(perform, status().isNotFound(), getMessage("notFound"),
                getMessage("notFound.message"), BASE_PATH + "/1");
    }
}