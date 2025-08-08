package com.example.product_service.controller.auth;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.common.advice.CustomAccessDeniedHandler;
import com.example.product_service.common.advice.CustomAuthenticationEntryPoint;
import com.example.product_service.config.WebSecurity;
import com.example.product_service.controller.OptionTypeController;
import com.example.product_service.controller.util.UserRole;
import com.example.product_service.dto.request.options.OptionTypeRequest;
import com.example.product_service.dto.request.options.OptionValueRequest;
import com.example.product_service.service.OptionTypeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static com.example.product_service.controller.util.ControllerTestHelper.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(OptionTypeController.class)
@Import({WebSecurity.class, CustomAccessDeniedHandler.class, CustomAuthenticationEntryPoint.class, MessageSourceUtil.class})
@AutoConfigureMockMvc
public class OptionTypeControllerSecurityTest {

    private static final String BASE_PATH = "/option-types";
    private static final String OPTION_TYPE_ID_PATH = BASE_PATH + "/1";
    private static final String CREATE_OPTION_VALUE_PATH = BASE_PATH + "/1/option-values";

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    OptionTypeService optionTypeService;

    @Test
    @DisplayName("옵션 타입 저장 테스트-인증 에러")
    void createOptionTypeTest_UnAuthorized() throws Exception {
        ResultActions perform =
                performWithBody(mockMvc, post(BASE_PATH), createOptionTypeRequest());
        verifyUnauthorizedResponse(perform, BASE_PATH);
    }

    @Test
    @DisplayName("옵션 타입 저장 테스트-권한 부족")
    void createOptionTypeTest_NoPermission() throws Exception {
        ResultActions perform =
                performWithAuthAndBody(mockMvc, post(BASE_PATH), createOptionTypeRequest(), UserRole.ROLE_USER);
        verifyNoPermissionResponse(perform, BASE_PATH);
    }

    @Test
    @DisplayName("옵션 값 저장 테스트-인증 에러")
    void createOptionValueTest_UnAuthorized() throws Exception {
        ResultActions perform =
                performWithBody(mockMvc, post(CREATE_OPTION_VALUE_PATH), createOptionValueRequest());
        verifyUnauthorizedResponse(perform, CREATE_OPTION_VALUE_PATH);
    }

    @Test
    @DisplayName("옵션 값 저장 테스트-권한 부족")
    void createOptionValueTest_NoPermission() throws Exception {
        ResultActions perform =
                performWithAuthAndBody(mockMvc, post(CREATE_OPTION_VALUE_PATH), createOptionValueRequest(), UserRole.ROLE_USER);
        verifyNoPermissionResponse(perform, CREATE_OPTION_VALUE_PATH);
    }

    @Test
    @DisplayName("옵션 타입 수정 테스트-인증 에러")
    void updateOptionTypeTest_UnAuthorized() throws Exception {
        ResultActions perform =
                performWithBody(mockMvc, patch(OPTION_TYPE_ID_PATH), createOptionTypeRequest());
        verifyUnauthorizedResponse(perform, OPTION_TYPE_ID_PATH);
    }

    @Test
    @DisplayName("옵션 타입 수정 테스트-권한 부족")
    void updateOptionTypeTest_NoPermission() throws Exception {
        ResultActions perform =
                performWithAuthAndBody(mockMvc, patch(OPTION_TYPE_ID_PATH), createOptionTypeRequest(), UserRole.ROLE_USER);
        verifyNoPermissionResponse(perform, OPTION_TYPE_ID_PATH);
    }

    @Test
    @DisplayName("옵션 타입 삭제 테스트-인증 에러")
    void deleteOptionTypeTest_UnAuthorized() throws Exception {
        ResultActions perform = performWithBody(mockMvc, delete(OPTION_TYPE_ID_PATH), null);
        verifyUnauthorizedResponse(perform, OPTION_TYPE_ID_PATH);
    }

    @Test
    @DisplayName("옵션 타입 삭제 테스트-권한 부족")
    void deleteOptionTypeTest_NoPermission() throws Exception {
        ResultActions perform =
                performWithAuthAndBody(mockMvc, delete(OPTION_TYPE_ID_PATH), null, UserRole.ROLE_USER);
        verifyNoPermissionResponse(perform, OPTION_TYPE_ID_PATH);
    }

    private OptionTypeRequest createOptionTypeRequest() {
        return new OptionTypeRequest("name");
    }

    private OptionValueRequest createOptionValueRequest() {
        return new OptionValueRequest("value");
    }
}
