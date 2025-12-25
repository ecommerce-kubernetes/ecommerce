package com.example.order_service.docs.notification;

import com.example.order_service.api.common.security.model.UserRole;
import com.example.order_service.api.common.security.principal.UserPrincipal;
import com.example.order_service.api.notification.controller.NotificationController;
import com.example.order_service.api.notification.service.NotificationService;
import com.example.order_service.docs.RestDocSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class NotificationControllerDocsTest extends RestDocSupport {
    private NotificationService notificationService = mock(NotificationService.class);
    @Override
    protected Object initController() {
        return new NotificationController(notificationService);
    }

    @Override
    protected HandlerMethodArgumentResolver[] getArgumentResolvers() {
        return new HandlerMethodArgumentResolver[]{
                new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.getParameterType().equals(UserPrincipal.class);
                    }

                    @Override
                    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                        // 헤더에서 ID를 읽어 가짜 UserPrincipal 생성 (given 조건과 일치하도록 1L 반환)
                        return UserPrincipal.builder().userId(1L).userRole(UserRole.ROLE_USER).build();
                    }
                }
        };
    }

    @Test
    @DisplayName("주문 Sse 메시지")
    void subscribe() throws Exception {
        //given
        HttpHeaders roleUser = createUserHeader("ROLE_USER");
        SseEmitter sseEmitter = new SseEmitter();
        given(notificationService.createEmitter(anyLong()))
                .willReturn(sseEmitter);
        //when
        //then
        MvcResult mvcResult = mockMvc.perform(get("/notification/subscribe")
                        .headers(roleUser)
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andDo(
                        document("sse-subscribe",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("X-User-Id").description(USER_ID_HEADER_DESCRIPTION).optional(),
                                        headerWithName("X-User-Role").description(USER_ROLE_HEADER_DESCRIPTION).optional()
                                )
                        )
                )
                .andReturn();
        sseEmitter.complete();
        mockMvc.perform(asyncDispatch(mvcResult));
    }

    private HttpHeaders createUserHeader(String userRole){
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Id", "1");
        headers.add("X-User-Role", userRole);
        return headers;
    }
}
