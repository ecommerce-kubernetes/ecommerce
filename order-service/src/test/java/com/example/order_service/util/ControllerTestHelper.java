package com.example.order_service.util;

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

import java.util.Iterator;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public final class ControllerTestHelper {
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);;
    private ControllerTestHelper(){
    }
    public static ResultActions performWithBodyAndUserIdHeader(MockMvc mockMvc, MockHttpServletRequestBuilder builder,
                                                Object bodyObject) throws Exception {
        builder.header("X-User-Id", 1L);
        jsonBodyMapping(builder, bodyObject);
        return mockMvc.perform(builder);
    }

    public static String toJson(Object o) throws JsonProcessingException {
        return mapper.writeValueAsString(o);
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

    private static void verifyJsonNodeNotExist(ResultActions perform, String jsonPath) throws Exception {
        perform.andExpect(jsonPath(jsonPath).doesNotExist());
    }

    private static void verifyJsonNodeNumberValue(ResultActions perform, String jsonPath, JsonNode node) throws Exception {
        perform.andExpect(jsonPath(jsonPath).value(node.numberValue()));
    }

    private static void verifyJsonNodeBooleanValue(ResultActions perform, String jsonPath, JsonNode node) throws Exception {
        perform.andExpect(jsonPath(jsonPath).value(node.booleanValue()));
    }

    private static void verifyJsonNodeEtcValue(ResultActions perform, String jsonPath, JsonNode node) throws Exception {
        perform.andExpect(jsonPath(jsonPath).value(node.asText()));
    }

    private static void jsonBodyMapping(MockHttpServletRequestBuilder builder, Object bodyObject) throws JsonProcessingException {
        if (bodyObject != null){
            String jsonBody = toJson(bodyObject);
            builder.contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody);
        }
    }

}
