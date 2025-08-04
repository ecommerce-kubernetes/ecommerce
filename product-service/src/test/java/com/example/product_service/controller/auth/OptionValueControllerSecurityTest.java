package com.example.product_service.controller.auth;

import com.example.product_service.common.advice.CustomAccessDeniedHandler;
import com.example.product_service.common.advice.CustomAuthenticationEntryPoint;
import com.example.product_service.config.WebSecurity;
import com.example.product_service.controller.OptionValueController;
import com.example.product_service.dto.request.options.OptionValueRequest;
import com.example.product_service.dto.request.options.UpdateOptionValueRequest;
import com.example.product_service.service.OptionValueService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static com.example.product_service.controller.util.SecurityTestHelper.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(OptionValueController.class)
@Import({WebSecurity.class, CustomAccessDeniedHandler.class, CustomAuthenticationEntryPoint.class})
@AutoConfigureMockMvc
public class OptionValueControllerSecurityTest {
    @Autowired
    MockMvc mockMvc;
    private static final String BASE_PATH = "/option-values";
    private static final String OPTION_VALUE_ID_PATH = "/option-values/1";
    @MockitoBean
    OptionValueService optionValueService;

    @Test
    @DisplayName("옵션 값 저장 테스트-인증 에러")
    void createOptionValueTest_UnAuthorized() throws Exception {
        String jsonBody = toJson(createOptionValueRequest());

        ResultActions perform = mockMvc.perform(post(BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody));

        verifyUnauthorizedResponse(perform, BASE_PATH);
    }

    @Test
    @DisplayName("옵션 값 저장 테스트-권한 부족")
    void createOptionValueTest_NoPermission() throws Exception {
        String jsonBody = toJson(createOptionValueRequest());

        ResultActions perform = mockMvc.perform(post(BASE_PATH)
                .header(USER_ID_HEADER, 1L)
                .header(USER_ROLE_HEADER, USER_ROLE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody));

        verifyNoPermissionResponse(perform, BASE_PATH);
    }

    @Test
    @DisplayName("옵션 값 수정 테스트-인증 에러")
    void updateOptionValueTest_UnAuthorized() throws Exception {
        String jsonBody = toJson(createUpdateOptionValueRequest());

        ResultActions perform = mockMvc.perform(patch(OPTION_VALUE_ID_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody));

        verifyUnauthorizedResponse(perform, OPTION_VALUE_ID_PATH);
    }

    @Test
    @DisplayName("옵션 값 수정 테스트-권한 부족")
    void updateOptionValueTest_NoPermission() throws Exception {
        String jsonBody = toJson(createUpdateOptionValueRequest());

        ResultActions perform = mockMvc.perform(patch(OPTION_VALUE_ID_PATH)
                        .header(USER_ID_HEADER, 1L)
                        .header(USER_ROLE_HEADER, USER_ROLE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody));

        verifyNoPermissionResponse(perform, OPTION_VALUE_ID_PATH);
    }

    @Test
    @DisplayName("옵션 값 삭제 테스트-인증 에러")
    void deleteOptionValueTest_UnAuthorized() throws Exception {
        ResultActions perform = mockMvc.perform(delete(OPTION_VALUE_ID_PATH));

        verifyUnauthorizedResponse(perform, OPTION_VALUE_ID_PATH);
    }

    @Test
    @DisplayName("옵션 값 삭제 테스트-권한 부족")
    void deleteOptionValueTest_NoPermission() throws Exception {
        ResultActions perform = mockMvc.perform(delete(OPTION_VALUE_ID_PATH)
                .header(USER_ID_HEADER, 1L)
                .header(USER_ROLE_HEADER, USER_ROLE));

        verifyNoPermissionResponse(perform, OPTION_VALUE_ID_PATH);
    }

    private static OptionValueRequest createOptionValueRequest() {
        return new OptionValueRequest(1L, "value");
    }

    private static UpdateOptionValueRequest createUpdateOptionValueRequest() {
        return new UpdateOptionValueRequest(1L, "value");
    }
}
