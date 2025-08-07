package com.example.product_service.controller.auth;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.common.advice.CustomAccessDeniedHandler;
import com.example.product_service.common.advice.CustomAuthenticationEntryPoint;
import com.example.product_service.config.WebSecurity;
import com.example.product_service.controller.OptionValueController;
import com.example.product_service.controller.util.UserRole;
import com.example.product_service.dto.request.options.OptionValueRequest;
import com.example.product_service.service.OptionValueService;
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

@WebMvcTest(OptionValueController.class)
@Import({WebSecurity.class, CustomAccessDeniedHandler.class, CustomAuthenticationEntryPoint.class, MessageSourceUtil.class})
@AutoConfigureMockMvc
public class OptionValueControllerSecurityTest {
    private static final String CREATE_OPTION_VALUE_PATH = "/option-types/1/option-values";
    private static final String BASE_PATH = "/option-values";
    private static final String OPTION_VALUE_ID_PATH = BASE_PATH + "/1";

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    OptionValueService optionValueService;

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
    @DisplayName("옵션 값 수정 테스트-인증 에러")
    void updateOptionValueTest_UnAuthorized() throws Exception {
        ResultActions perform =
                performWithBody(mockMvc, patch(OPTION_VALUE_ID_PATH), createOptionValueRequest());
        verifyUnauthorizedResponse(perform, OPTION_VALUE_ID_PATH);
    }

    @Test
    @DisplayName("옵션 값 수정 테스트-권한 부족")
    void updateOptionValueTest_NoPermission() throws Exception {
        ResultActions perform =
                performWithAuthAndBody(mockMvc, patch(OPTION_VALUE_ID_PATH), createOptionValueRequest(), UserRole.ROLE_USER);
        verifyNoPermissionResponse(perform, OPTION_VALUE_ID_PATH);
    }

    @Test
    @DisplayName("옵션 값 삭제 테스트-인증 에러")
    void deleteOptionValueTest_UnAuthorized() throws Exception {
        ResultActions perform =
                performWithBody(mockMvc, delete(OPTION_VALUE_ID_PATH), null);
        verifyUnauthorizedResponse(perform, OPTION_VALUE_ID_PATH);
    }

    @Test
    @DisplayName("옵션 값 삭제 테스트-권한 부족")
    void deleteOptionValueTest_NoPermission() throws Exception {
        ResultActions perform =
                performWithAuthAndBody(mockMvc, delete(OPTION_VALUE_ID_PATH), null, UserRole.ROLE_USER);
        verifyNoPermissionResponse(perform, OPTION_VALUE_ID_PATH);
    }

    private OptionValueRequest createOptionValueRequest() {
        return new OptionValueRequest("value");
    }

}
