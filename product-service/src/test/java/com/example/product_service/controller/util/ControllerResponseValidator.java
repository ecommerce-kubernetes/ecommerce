package com.example.product_service.controller.util;


import com.example.product_service.common.advice.dto.DetailError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.test.web.servlet.ResultActions;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestComponent
@Slf4j
public class ControllerResponseValidator {

    public void verifyErrorResponse(ResultActions perform,
                                     int expectedStatus,
                                     String expectedError,
                                     String expectedMessage,
                                     String requestUrl) throws Exception {
        perform
                .andExpect(status().is(expectedStatus))
                .andExpect(jsonPath("$.error").value(expectedError))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.path").value(requestUrl));
    }

    public void validInvalidFields(ResultActions perform,
                                    int expectedStatus,
                                    String expectedError,
                                    String expectedMessage,
                                    List<DetailError> expectedDetail,
                                    String requestUrl) throws Exception {
        perform
                .andExpect(status().is(expectedStatus))
                .andExpect(jsonPath("$.error").value(expectedError))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.path").value(requestUrl));

        for (DetailError detailError : expectedDetail) {
            perform
                    .andExpect(jsonPath("$.errors[*].fieldName").value(detailError.getFieldName()))
                    .andExpect(jsonPath("$.errors[*].message").value(detailError.getMessage()));
        }
    }

    public void validResponse(ResultActions perform,
                               Class<?> responseClass,
                               Object verifyResponse) throws Exception {
        if (verifyResponse instanceof Collection) {
            Collection<?> list = (Collection<?>) verifyResponse;
            int idx = 0;
            for (Object item : list) {
                validResponseObject(perform, item.getClass(), item, "$[" + (idx++) + "]");
            }
        } else {
            validResponseObject(perform, responseClass, verifyResponse, "$" );
        }
    }

    private void validResponseObject(ResultActions perform,
                                     Class<?> responseClass,
                                     Object verifyResponse,
                                     String jsonPathPrefix) throws Exception {
        for (Field field : responseClass.getDeclaredFields()) {
            field.setAccessible(true);
            String fieldName = field.getName();
            Object expectedValue = field.get(verifyResponse);
            perform.andExpect(jsonPath(jsonPathPrefix + "." + fieldName).value(expectedValue));
        }
    }
}
