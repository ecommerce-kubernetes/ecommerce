package com.example.product_service.controller;

import com.example.product_service.controller.util.TestMessageUtil;
import com.example.product_service.dto.request.options.OptionTypeRequest;
import com.example.product_service.dto.response.options.OptionTypeResponse;
import com.example.product_service.exception.DuplicateResourceException;
import com.example.product_service.service.OptionTypeService;
import lombok.extern.slf4j.Slf4j;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    OptionTypeService service;

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
    @DisplayName("옵션 타입 생성 테스트-실패(검증 예외)")
    void createOptionTypeTest_validation() throws Exception {
        OptionTypeRequest request = new OptionTypeRequest();
        ResultActions perform = performWithBody(mockMvc, post(BASE_PATH), request);
        verifyErrorResponse(perform, status().isBadRequest(),
                getMessage("badRequest"), getMessage("validation"), BASE_PATH);

        perform.andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    @DisplayName("옵션타입 생성 테스트-실패(동일 이름 존재시)")
    void createOptionTypeTest_conflict() throws Exception {
        String duplicateName = "duplicateName";
        OptionTypeRequest request = new OptionTypeRequest(duplicateName);
        when(service.saveOptionTypes(any(OptionTypeRequest.class)))
                .thenThrow(new DuplicateResourceException(getMessage("option-type.conflict")));

        ResultActions perform = performWithBody(mockMvc, post(BASE_PATH), request);
        verifyErrorResponse(perform, status().isConflict(), getMessage("conflict")
                ,getMessage("option-type.conflict"),BASE_PATH);

    }
}