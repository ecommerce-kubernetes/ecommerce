package com.example.product_service.controller;

import com.example.product_service.dto.request.options.OptionTypeRequest;
import com.example.product_service.service.OptionTypeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OptionTypeController.class)
@Slf4j
@AutoConfigureMockMvc(addFilters = false)
class OptionTypeControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    OptionTypeService service;

    ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("옵션 타입 생성 테스트-성공")
    void createOptionTypeTest_success() throws Exception {
        OptionTypeRequest request = new OptionTypeRequest("new OptionType");
        String jsonBody = mapper.writeValueAsString(request);

        ResultActions perform = mockMvc.perform(post("/option-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody));

        perform.andExpect(status().isCreated());
    }
}