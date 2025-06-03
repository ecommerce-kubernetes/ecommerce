package com.example.product_service.controller.util;


import com.example.product_service.common.advice.dto.DetailError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.test.web.servlet.ResultActions;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        // 루트가 컬렉션인 경우 각 요소를 인덱스별로 검증
        if (verifyResponse instanceof Collection) {
            Collection<?> list = (Collection<?>) verifyResponse;
            int idx = 0;
            for (Object item : list) {
                validResponseObject(perform,
                        item == null ? responseClass : item.getClass(),
                        item,
                        "$[" + (idx++) + "]",
                        new HashSet<>());
            }
        } else {
            validResponseObject(perform,
                    responseClass,
                    verifyResponse,
                    "$",
                    new HashSet<>());
        }
    }

    private void validResponseObject(ResultActions perform,
                                     Class<?> responseClass,
                                     Object verifyResponse,
                                     String jsonPathPrefix,
                                     Set<Object> visited) throws Exception {
        if (verifyResponse == null || visited.contains(verifyResponse)) {
            return;
        }
        visited.add(verifyResponse);

        for (Field field : responseClass.getDeclaredFields()) {
            field.setAccessible(true);
            Object expectedValue = field.get(verifyResponse);
            String fieldName = field.getName();
            String currentPath = jsonPathPrefix + "." + fieldName;

            if (expectedValue == null) {
                perform.andExpect(jsonPath(currentPath).doesNotExist());
                continue;
            }

            Class<?> fieldType = field.getType();
            if (LocalDateTime.class.isAssignableFrom(fieldType)) {
                String formatted = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format((LocalDateTime) expectedValue);
                perform.andExpect(jsonPath(currentPath).value(formatted));  // "2025-05-25T13:20:00"
                continue;
            }
            if (isPrimitiveOrWrapper(fieldType) || String.class.isAssignableFrom(fieldType)) {
                perform.andExpect(jsonPath(currentPath).value(expectedValue));
                continue;
            }
            // 컬렉션인 경우
            if (expectedValue instanceof Collection) {
                Collection<?> collection = (Collection<?>) expectedValue;
                int idx = 0;
                for (Object elem : collection) {
                    String elementPath = currentPath + "[" + idx + "]";
                    if (elem == null) {
                        perform.andExpect(jsonPath(elementPath).doesNotExist());
                    } else if (isPrimitiveOrWrapper(elem.getClass()) || elem instanceof String) {
                        perform.andExpect(jsonPath(elementPath).value(elem));
                    } else if (LocalDateTime.class.isAssignableFrom(elem.getClass())) {
                        perform.andExpect(jsonPath(elementPath).value(elem.toString()));
                    } else {
                        // 재귀 호출
                        validResponseObject(perform,
                                elem.getClass(),
                                elem,
                                elementPath,
                                visited);
                    }
                    idx++;
                }
                continue;
            }
            // 나머지 (커스텀 DTO 등)
            validResponseObject(perform,
                    expectedValue.getClass(),
                    expectedValue,
                    currentPath,
                    visited);
        }
    }


    private boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive()
                || clazz == Boolean.class
                || clazz == Byte.class
                || clazz == Character.class
                || clazz == Short.class
                || clazz == Integer.class
                || clazz == Long.class
                || clazz == Double.class
                || clazz == Float.class;
    }
}
