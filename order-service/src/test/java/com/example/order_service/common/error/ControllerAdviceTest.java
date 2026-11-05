package com.example.order_service.common.error;

import com.example.order_service.support.DummyController;
import com.example.order_service.support.annotation.WithCustomMockUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DummyController.class)
public class ControllerAdviceTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @DisplayName("BusinessException 발생시 에러 코드에 정의된 상태 코드와 메시지가 반환된다")
    @WithCustomMockUser
    void handleBusinessException_NotFound() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(get("/exception")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("code").value("ORDER_NOT_FOUND"))
                .andExpect(jsonPath("message").value("주문을 찾을 수 없습니다"))
                .andExpect(jsonPath("timestamp").isNotEmpty())
                .andExpect(jsonPath("path").value("/exception"));
    }

    @Test
    @DisplayName("PortException 발생시 에러 코드의 카테고리에 매핑된 상태 코드와 메시지가 반환된다")
    @WithCustomMockUser
    void handlePortException() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(get("/exception/port")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("code").value("PRODUCT_CIRCUIT_OPEN"))
                .andExpect(jsonPath("message").value("상품 연동이 일시적으로 지연되고 있습니다. 잠시 후 다시 시도해주세요."))
                .andExpect(jsonPath("timestamp").isNotEmpty())
                .andExpect(jsonPath("path").value("/exception/port"));
    }
}
