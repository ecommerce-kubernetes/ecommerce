package com.example.product_service.common.error;

import com.example.product_service.support.ExceptionTestController;
import com.example.product_service.support.security.config.TestSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({TestSecurityConfig.class, ExceptionTestController.class})
@WebMvcTest(controllers = ExceptionTestController.class)
class ControllerAdviceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("BusinessException 발생시 에러 코드에 정의된 상태 코드와 메시지가 반환된다")
    void handleBusinessException() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(get("/exception/business")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("code").value("CATEGORY_001"))
                .andExpect(jsonPath("message").value("카테고리를 찾을 수 없습니다"))
                .andExpect(jsonPath("timestamp").isNotEmpty())
                .andExpect(jsonPath("path").value("/exception/business"));
    }

    @Test
    @DisplayName("MethodArgumentNotValidException 발생시 검증 실패 필드가 errors에 담겨 반환된다")
    void handleMethodArgumentNotValidException() throws Exception {
        //given
        ExceptionTestController.ValidationTestRequest request = ExceptionTestController.ValidationTestRequest.builder()
                .name(null)
                .build();
        //when
        //then
        mockMvc.perform(post("/exception/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("message").value("입력값이 올바르지 않습니다."))
                .andExpect(jsonPath("errors[0].field").value("name"))
                .andExpect(jsonPath("errors[0].reason").value("이름은 필수입니다"))
                .andExpect(jsonPath("timestamp").isNotEmpty())
                .andExpect(jsonPath("path").value("/exception/validation"));
    }
}
