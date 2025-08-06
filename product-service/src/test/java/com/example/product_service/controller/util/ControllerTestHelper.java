package com.example.product_service.controller.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class ControllerTestHelper {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_ROLE_HEADER = "X-User-Role";
    public static final String USER_ROLE = UserRole.ROLE_USER.name();
    public static final String ADMIN_ROLE = UserRole.ROLE_ADMIN.name();

    private ControllerTestHelper(){
    }

    public static String toJson(Object o) throws JsonProcessingException {
        return mapper.writeValueAsString(o);
    }

    public static void verifyNoPermissionResponse(ResultActions perform, String path) throws Exception {
        verityErrorResponse(perform, status().isForbidden(), "Forbidden", "Access Denied", path);
    }
    public static void verifyUnauthorizedResponse(ResultActions perform, String path) throws Exception {
        verityErrorResponse(perform, status().isUnauthorized(), "UnAuthorized", "Invalid Header", path);
    }

    /**
     * 검증객체 생성 편의 메서드
     * @param mockMvc 테스트용 MockMvc
     * @param builder post(), patch() 등 생성한 MockHttpServletRequestBuilder
     * @param bodyObject 요청 바디로 직렬화할 DTO (없으면 null)
     * @param userRole X-User-Id , X-User-Role 헤더 생성 (User ,Admin)
     * @return 생성된 검증 객체
     */
    public static ResultActions performWithAuthAndBody(MockMvc mockMvc, MockHttpServletRequestBuilder builder,
                                                       Object bodyObject,
                                                       UserRole userRole) throws Exception {
        jsonBodyMapping(builder, bodyObject);

        if (userRole == UserRole.ROLE_USER){
            builder
                    .header(USER_ID_HEADER, 1)
                    .header(USER_ROLE_HEADER, USER_ROLE);
        } else if (userRole == UserRole.ROLE_ADMIN){
            builder
                    .header(USER_ID_HEADER, 1)
                    .header(USER_ROLE_HEADER, ADMIN_ROLE);
        }

        return mockMvc.perform(builder);
    }

    public static ResultActions performWithBody(MockMvc mockMvc, MockHttpServletRequestBuilder builder,
                                                Object bodyObject) throws Exception {
        jsonBodyMapping(builder, bodyObject);
        return mockMvc.perform(builder);
    }

    private static void verityErrorResponse(ResultActions perform, ResultMatcher status, String error, String message, String path) throws Exception {
        perform
                .andExpect(status)
                .andExpect(jsonPath("$.error").value(error))
                .andExpect(jsonPath("$.message").value(message))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value(path));
    }

    private static void jsonBodyMapping(MockHttpServletRequestBuilder builder, Object bodyObject) throws JsonProcessingException {
        if (bodyObject != null){
            String jsonBody = toJson(bodyObject);
            builder.contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody);
        }
    }
}
