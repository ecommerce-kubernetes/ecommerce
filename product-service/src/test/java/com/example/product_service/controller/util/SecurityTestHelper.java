package com.example.product_service.controller.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class SecurityTestHelper {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_ROLE_HEADER = "X-User-Role";
    public static final String USER_ROLE = UserRole.ROLE_USER.name();
    public static final String ADMIN_ROLE = UserRole.ROLE_ADMIN.name();

    private SecurityTestHelper(){
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

    private static void verityErrorResponse(ResultActions perform, ResultMatcher status, String error, String message, String path) throws Exception {
        perform
                .andExpect(status)
                .andExpect(jsonPath("$.error").value(error))
                .andExpect(jsonPath("$.message").value(message))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value(path));
    }
}
