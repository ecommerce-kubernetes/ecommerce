package com.example.product_service.controller;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.request.options.OptionValueRequest;
import com.example.product_service.dto.response.options.OptionValueResponse;
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

import static com.example.product_service.controller.util.ControllerTestHelper.performWithBody;
import static com.example.product_service.controller.util.ControllerTestHelper.verifySuccessResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OptionValueController.class)
@AutoConfigureMockMvc(addFilters = false)
class OptionValueControllerTest {

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
    void createOptionValue_success() throws Exception {
        OptionValueRequest request = new OptionValueRequest(1L, "value");
        OptionValueResponse response = new OptionValueResponse(1L, 1L, "value");
        when(service.saveOptionValue(any(OptionValueRequest.class))).thenReturn(response);

        ResultActions perform = performWithBody(mockMvc, post("/option-values"), request);
        verifySuccessResponse(perform, status().isCreated(), response);
    }
}