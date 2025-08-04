package com.example.product_service.controller.auth;

import com.example.product_service.common.advice.CustomAccessDeniedHandler;
import com.example.product_service.common.advice.CustomAuthenticationEntryPoint;
import com.example.product_service.config.WebSecurity;
import com.example.product_service.controller.OptionTypeController;
import com.example.product_service.dto.request.ModifyCategoryRequest;
import com.example.product_service.dto.request.options.OptionTypeRequest;
import com.example.product_service.service.OptionTypeService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OptionTypeController.class)
@Import({WebSecurity.class, CustomAccessDeniedHandler.class, CustomAuthenticationEntryPoint.class})
@AutoConfigureMockMvc
public class OptionTypeControllerSecurityTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    OptionTypeService optionTypeService;

    ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("옵션 타입 테스트-인증 에러")
    void createOptionTypeTest_UnAuthorized() throws Exception {
        String jsonBody = createModifyCategoryRequestJsonBody();

        ResultActions perform = mockMvc.perform(post("/option-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody));

        perform
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UnAuthorized"))
                .andExpect(jsonPath("$.message").value("Invalid Header"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/option-types"));
    }

    private String createModifyCategoryRequestJsonBody() throws JsonProcessingException {
        OptionTypeRequest request = new OptionTypeRequest("name");
        return createJsonBody(request);
    }

    private String createJsonBody(Object o) throws JsonProcessingException {
        return mapper.writeValueAsString(o);
    }

}
