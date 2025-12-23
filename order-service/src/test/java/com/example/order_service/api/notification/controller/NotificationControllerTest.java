package com.example.order_service.api.notification.controller;

import com.example.order_service.api.support.ControllerTestSupport;
import com.example.order_service.api.support.security.annotation.WithCustomMockUser;
import com.example.order_service.api.support.security.config.TestSecurityConfig;
import com.example.order_service.config.TestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

@Import({TestConfig.class, TestSecurityConfig.class})
public class NotificationControllerTest extends ControllerTestSupport {
    
    @Test
    @DisplayName("")
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
}
