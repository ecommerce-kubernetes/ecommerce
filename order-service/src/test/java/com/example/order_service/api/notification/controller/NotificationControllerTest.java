package com.example.order_service.api.notification.controller;

import com.example.order_service.api.notification.listener.dto.OrderNotificationDto;
import com.example.order_service.api.support.ControllerTestSupport;
import com.example.order_service.api.support.security.annotation.WithCustomMockUser;
import com.example.order_service.api.support.security.config.TestSecurityConfig;
import com.example.order_service.config.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import({TestConfig.class, TestSecurityConfig.class})
public class NotificationControllerTest extends ControllerTestSupport {

    @BeforeEach
    void setUp(WebApplicationContext context) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(new CharacterEncodingFilter("UTF-8", true)) // UTF-8 필터 추가
                .build();
    }

    @Test
    @DisplayName("SSE 연결시 에미터가 생성")
    @WithCustomMockUser
    void subscribe() throws Exception {
        //given
        Long userId = 1L;
        SseEmitter sseEmitter = new SseEmitter();
        given(notificationService.createEmitter(anyLong()))
                .willReturn(sseEmitter);
        //when
        //then
        MvcResult mvcResult = mockMvc.perform(get("/notification/subscribe"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();
        sseEmitter.complete();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_EVENT_STREAM_VALUE));

        verify(notificationService).createEmitter(userId);
    }

    @Test
    @DisplayName("SSE 구독 후 메시지가 전송되면 올바른 JSON 포맷을 가진다")
    @WithCustomMockUser
    void subscribe_data_format_test() throws Exception {
        //given
        SseEmitter sseEmitter = new SseEmitter();
        given(notificationService.createEmitter(anyLong())).willReturn(sseEmitter);

        //when
        MvcResult mvcResult = mockMvc.perform(get("/notification/subscribe"))
                .andExpect(request().asyncStarted())
                .andReturn();
        OrderNotificationDto dto = OrderNotificationDto.builder()
                .status("SUCCESS").message("결제 대기").build();

        sseEmitter.send(SseEmitter.event().name("ORDER_RESULT").data(dto));
        sseEmitter.complete();

        //then
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("event:ORDER_RESULT")))
                .andExpect(content().string(containsString("\"status\":\"SUCCESS\"")))
                .andExpect(content().string(containsString("\"message\":\"결제 대기\"")));
    }
}
