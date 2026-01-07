package com.example.product_service.api.common.util;

import com.example.product_service.api.common.config.JacksonConfig;
import com.example.product_service.api.support.ControllerTestSupport;
import com.example.product_service.api.support.DummyController;
import com.example.product_service.api.support.security.config.TestSecurityConfig;
import com.example.product_service.config.TestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Import({TestConfig.class, TestSecurityConfig.class, JacksonConfig.class})
public class StringTrimmerTest extends ControllerTestSupport {
    @Test
    @DisplayName("")
    void trim() throws Exception {
        //given
        DummyController.TestRequest request = new DummyController.TestRequest("  공백 테스트");
        //when
        //then
        mockMvc.perform(post("/test")
                        .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("공백 테스트"));
    }
}
