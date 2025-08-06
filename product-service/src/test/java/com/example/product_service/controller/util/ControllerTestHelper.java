package com.example.product_service.controller.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Iterator;
import java.util.Map;

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
        verifyErrorResponse(perform, status().isForbidden(), "Forbidden", "Access Denied", path);
    }
    public static void verifyUnauthorizedResponse(ResultActions perform, String path) throws Exception {
        verifyErrorResponse(perform, status().isUnauthorized(), "UnAuthorized", "Invalid Header", path);
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

    public static void verifyErrorResponse(ResultActions perform, ResultMatcher status, String error, String message, String path) throws Exception {
        perform
                .andExpect(status)
                .andExpect(jsonPath("$.error").value(error))
                .andExpect(jsonPath("$.message").value(message))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value(path));
    }

    public static void verifySuccessResponse(ResultActions perform, ResultMatcher status, Object response) throws Exception {

        perform.andExpect(status);

        JsonNode expected = mapper.valueToTree(response);

        Iterator<Map.Entry<String, JsonNode>> fields = expected.fields();
        assertJsonNode(perform, "$", expected);
    }
    private static void assertJsonNode(ResultActions perform, String jsonPath, JsonNode node) throws Exception {
        if(node.isNull()){
            perform.andExpect(jsonPath(jsonPath).doesNotExist());
        } else if (node.isNumber()){
            perform.andExpect(jsonPath(jsonPath).value(node.numberValue()));
        } else if (node.isBoolean()){
            perform.andExpect(jsonPath(jsonPath).value(node.booleanValue()));
        } else if (node.isObject()){
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while(fields.hasNext()){
                Map.Entry<String, JsonNode> entry = fields.next();
                String childPath = String.format("%s.%s", jsonPath, entry.getKey());
                assertJsonNode(perform, childPath, entry.getValue());
            }
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                String elementPath = String.format("%s[%d]", jsonPath, i);
                assertJsonNode(perform, elementPath, node.get(i));
            }
        } else {
            perform.andExpect(jsonPath(jsonPath).value(node.asText()));
        }
    }

    private static void jsonBodyMapping(MockHttpServletRequestBuilder builder, Object bodyObject) throws JsonProcessingException {
        if (bodyObject != null){
            String jsonBody = toJson(bodyObject);
            builder.contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody);
        }
    }
}
