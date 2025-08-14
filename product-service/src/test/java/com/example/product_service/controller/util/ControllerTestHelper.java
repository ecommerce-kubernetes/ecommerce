package com.example.product_service.controller.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class ControllerTestHelper {

    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);;

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
    public static ResultActions performWithPageRequest(MockMvc mockMvc, MockHttpServletRequestBuilder builder,
                                                       Integer page, Integer size, List<String> sort, Map<String, String> extras) throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        if(page != null) params.add("page", page.toString());
        if(size != null) params.add("size", size.toString());
        if(sort != null) sort.forEach(s -> params.add("sort", s));
        if(extras != null) extras.forEach(params::add);
        return performWithParams(mockMvc, builder, params);
    }

    public static ResultActions performWithParams(MockMvc mockMvc, MockHttpServletRequestBuilder builder,
                                                 MultiValueMap<String, String> paramsMap) throws Exception {
        setParams(builder, paramsMap);
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
        assertJsonNode(perform, "$", expected);
    }

    private static void assertJsonNode(ResultActions perform, String jsonPath, JsonNode node) throws Exception {
        if(node.isNull()){
            verifyJsonNodeNotExist(perform, jsonPath);
        } else if (node.isNumber()){
            verifyJsonNodeNumberValue(perform, jsonPath, node);
        } else if (node.isBoolean()){
            verifyJsonNodeBooleanValue(perform, jsonPath, node);
        } else if (node.isObject()){
            processingObjectNode(perform, jsonPath, node);
        } else if (node.isArray()) {
            processingArrayNode(perform, jsonPath, node);
        } else {
            verifyJsonNodeEtcValue(perform, jsonPath, node);
        }
    }

    private static void processingObjectNode(ResultActions perform, String jsonPath, JsonNode node) throws Exception {
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while(fields.hasNext()){
            Map.Entry<String, JsonNode> entry = fields.next();
            String childPath = String.format("%s.%s", jsonPath, entry.getKey());
            assertJsonNode(perform, childPath, entry.getValue());
        }
    }

    private static void processingArrayNode(ResultActions perform, String jsonPath, JsonNode node) throws Exception {
        for (int i = 0; i < node.size(); i++) {
            String elementPath = String.format("%s[%d]", jsonPath, i);
            assertJsonNode(perform, elementPath, node.get(i));
        }
    }

    private static void verifyJsonNodeEtcValue(ResultActions perform, String jsonPath, JsonNode node) throws Exception {
        perform.andExpect(jsonPath(jsonPath).value(node.asText()));
    }

    private static void verifyJsonNodeBooleanValue(ResultActions perform, String jsonPath, JsonNode node) throws Exception {
        perform.andExpect(jsonPath(jsonPath).value(node.booleanValue()));
    }

    private static void verifyJsonNodeNumberValue(ResultActions perform, String jsonPath, JsonNode node) throws Exception {
        perform.andExpect(jsonPath(jsonPath).value(node.numberValue()));
    }

    private static void verifyJsonNodeNotExist(ResultActions perform, String jsonPath) throws Exception {
        perform.andExpect(jsonPath(jsonPath).doesNotExist());
    }

    private static void jsonBodyMapping(MockHttpServletRequestBuilder builder, Object bodyObject) throws JsonProcessingException {
        if (bodyObject != null){
            String jsonBody = toJson(bodyObject);
            builder.contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody);
        }
    }

    private static void setParams(MockHttpServletRequestBuilder builder, MultiValueMap<String, String> paramsMap){
        if(paramsMap != null){
            builder.params(paramsMap);
        }
    }
}
