package com.example.userservice.api.common.error;

import com.example.userservice.api.support.ControllerTestSupport;
import com.example.userservice.api.support.security.config.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestSecurityConfig.class)
class ControllerAdviceTest extends ControllerTestSupport {

    @Test
    @DisplayName("BusinessException 발생시 에러 코드에 정의된 상태 코드와 메시지가 반환된다")
    void handleBusinessException_NotFound() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(get("/exception")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("code").value("USER_001"))
                .andExpect(jsonPath("message").value("해당 유저를 찾을 수 없습니다"))
                .andExpect(jsonPath("timestamp").isNotEmpty())
                .andExpect(jsonPath("path").value("/exception"));
    }

    @Test
    @DisplayName("잘못된 형식의 요청 바디가 온 경우 INVALID_TYPE_VALUE 에러 응답을 반환한다")
    void handleMessageNotReadableException_date() throws Exception {
        String invalidJson = """
                    {
                        "datetime": "19991225"
                    }
                """;
        mockMvc.perform(post("/not-readable")
                .content(invalidJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value("COMMON_002"))
                .andExpect(jsonPath("message").value("잘못된 날짜 형식 입니다"))
                .andExpect(jsonPath("timestamp").isNotEmpty())
                .andExpect(jsonPath("path").value("/not-readable"));
    }

    @Test
    @DisplayName("잘못된 형식의 요청 바디가 온 경우 INVALID_TYPE_VALUE 에러 응답을 반환한다")
    void handleMessageNotReadableException_other() throws Exception {
        String invalidJson = """
                    {
                        "number": "str"
                    }
                """;
        mockMvc.perform(post("/not-readable")
                        .content(invalidJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value("COMMON_003"))
                .andExpect(jsonPath("message").value("요청 데이터 형식이 올바르지 않습니다"))
                .andExpect(jsonPath("timestamp").isNotEmpty())
                .andExpect(jsonPath("path").value("/not-readable"));
    }
}