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
public class ControllerAdviceTest extends ControllerTestSupport {

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
}
