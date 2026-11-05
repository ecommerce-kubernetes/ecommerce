package com.example.userservice.common.error;

import com.example.userservice.support.ExceptionTestController;
import com.example.userservice.support.security.config.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({TestSecurityConfig.class, ExceptionTestController.class})
@WebMvcTest(controllers = ExceptionTestController.class)
class ControllerAdviceTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("BusinessException 발생시 에러 코드에 정의된 상태 코드와 메시지가 반환된다")
    void handleBusinessException() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(get("/exception/business")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("code").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("message").value("해당 유저를 찾을 수 없습니다"))
                .andExpect(jsonPath("timestamp").isNotEmpty())
                .andExpect(jsonPath("path").value("/exception/business"));
    }

    @Test
    @DisplayName("PortException 발생시 에러코드에 정의된 상태 코드와 메시지가 반환된다")
    void handlePortException() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(get("/exception/port")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("code").value("AUTH_INVALID_CREDENTIALS"))
                .andExpect(jsonPath("message").value("아이디 또는 비밀번호를 확인해주세요."))
                .andExpect(jsonPath("timestamp").isNotEmpty())
                .andExpect(jsonPath("path").value("/exception/port"));
    }
}