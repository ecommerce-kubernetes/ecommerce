package com.example.product_service.api.option.controller;

import com.example.product_service.api.common.security.model.UserRole;
import com.example.product_service.api.option.controller.dto.OptionCreateRequest;
import com.example.product_service.api.option.controller.dto.OptionUpdateRequest;
import com.example.product_service.api.option.service.dto.OptionResponse;
import com.example.product_service.api.option.service.dto.OptionValueResponse;
import com.example.product_service.support.ControllerTestSupport;
import com.example.product_service.support.security.annotation.WithCustomMockUser;
import com.example.product_service.support.security.config.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestSecurityConfig.class)
public class OptionControllerTest extends ControllerTestSupport {


    @Test
    @DisplayName("옵션을 저장한다")
    @WithCustomMockUser
    void saveOption() throws Exception {
        //given
        OptionCreateRequest request = createOptionRequest().build();
        OptionResponse response = createOptionResponse().build();
        given(optionService.saveOption(anyString(), anyList()))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(post("/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("옵션을 저장하려면 관리자 권한이여야 한다")
    @WithCustomMockUser(userRole = UserRole.ROLE_USER)
    void saveOptionWithUserRole() throws Exception {
        //given
        OptionCreateRequest request = createOptionRequest().build();
        //when
        //then
        mockMvc.perform(post("/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("FORBIDDEN"))
                .andExpect(jsonPath("message").value("요청 권한이 부족합니다"))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/options"));
    }

    @Test
    @DisplayName("로그인 하지 않은 유저는 옵션을 저장할 수 없다")
    void saveOption_unAuthentication() throws Exception {
        //given
        OptionCreateRequest request = createOptionRequest().build();
        //when
        //then
        mockMvc.perform(post("/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("message").value("인증이 필요한 접근입니다"))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/options"));
    }

    @ParameterizedTest(name = "{0}")
    @DisplayName("옵션 저장 요청 검증")
    @MethodSource("provideInvalidRequest")
    @WithCustomMockUser
    void saveOption_validation(String description, OptionCreateRequest request, String message) throws Exception {
        //given
        //when
        //then
        mockMvc.perform(post("/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("code").value("VALIDATION"))
                .andExpect(jsonPath("message").value(message))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/options"));
    }

    @Test
    @DisplayName("옵션을 조회한다")
    void getOption() throws Exception {
        //given
        OptionResponse response = createOptionResponse().build();
        given(optionService.getOption(anyLong()))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(get("/options/{optionTypeId}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("옵션 목록을 조회한다")
    void getOptions() throws Exception {
        //given
        OptionResponse response = createOptionResponse().build();
        given(optionService.getOptions())
                .willReturn(List.of(response));
        //when
        //then
        mockMvc.perform(get("/options")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(response))));
    }

    @Test
    @DisplayName("옵션을 수정한다")
    @WithCustomMockUser
    void updateOptionType() throws Exception {
        //given
        OptionUpdateRequest request = createOptionUpdateRequest().build();
        OptionResponse response = createOptionResponse().build();
        given(optionService.updateOptionTypeName(anyLong(), anyString()))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(patch("/options/{optionTypeId}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("옵션을 수정하려면 관리자 권한이여야 한다")
    @WithCustomMockUser(userRole = UserRole.ROLE_USER)
    void updateOptionTypeWithUserRole() throws Exception {
        //given
        OptionCreateRequest request = createOptionRequest().build();
        //when
        //then
        mockMvc.perform(patch("/options/{optionTypeId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("FORBIDDEN"))
                .andExpect(jsonPath("message").value("요청 권한이 부족합니다"))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/options/1"));
    }

    @Test
    @DisplayName("로그인 하지 않은 회원은 옵션을 수정할 수 없다")
    void updateOption_Type_unAuthentication() throws Exception {
        //given
        OptionCreateRequest request = createOptionRequest().build();
        //when
        //then
        mockMvc.perform(patch("/options/{optionTypeId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("message").value("인증이 필요한 접근입니다"))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/options/1"));
    }

    @Test
    @DisplayName("옵션 수정 요청 검증")
    @WithCustomMockUser
    void updateOption_Type_validation() throws Exception {
        //given
        OptionUpdateRequest request = createOptionUpdateRequest().name(null).build();
        //when
        //then
        mockMvc.perform(patch("/options/{optionTypeId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("code").value("VALIDATION"))
                .andExpect(jsonPath("message").value("이름은 필수입니다"))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/options/1"));
    }

    @Test
    @DisplayName("옵션을 삭제한다")
    @WithCustomMockUser
    void deleteOption() throws Exception {
        //given
        willDoNothing().given(optionService).deleteOption(anyLong());
        //when
        //then
        mockMvc.perform(delete("/options/{optionTypeId}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("옵션을 삭제하려면 관리자 권한이여야 한다")
    @WithCustomMockUser(userRole = UserRole.ROLE_USER)
    void deleteOptionWithUserRole() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(delete("/options/{optionTypeId}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("FORBIDDEN"))
                .andExpect(jsonPath("message").value("요청 권한이 부족합니다"))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/options/1"));
    }

    @Test
    @DisplayName("로그인 하지 않은 회원은 옵션을 삭제할 수 없다")
    void deleteOption_unAuthentication() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(delete("/options/{optionTypeId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("message").value("인증이 필요한 접근입니다"))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/options/1"));
    }

    @Test
    @DisplayName("옵션 값을 수정한다")
    @WithCustomMockUser
    void updateOptionValue() throws Exception {
        //given
        OptionUpdateRequest request = createOptionUpdateRequest().build();
        OptionValueResponse response = createOptionValueResponse().build();
        given(optionService.updateOptionValueName(anyLong(), anyString()))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(patch("/option-values/{optionValueId}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("옵션 값을 변경하려면 관리자 권한이여야 한다")
    @WithCustomMockUser(userRole = UserRole.ROLE_USER)
    void updateOptionValue_user_role() throws Exception {
        //given
        OptionUpdateRequest request = createOptionUpdateRequest().build();
        //when
        //then
        mockMvc.perform(patch("/option-values/{optionValueId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("FORBIDDEN"))
                .andExpect(jsonPath("message").value("요청 권한이 부족합니다"))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/option-values/1"));
    }

    @Test
    @DisplayName("로그인 하지 않은 사용자는 옵션 값을 변경할 수 없다")
    void updateOptionValue_unAuthorized() throws Exception {
        //given
        OptionUpdateRequest request = createOptionUpdateRequest().build();
        //when
        //then
        mockMvc.perform(patch("/option-values/{optionValueId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("message").value("인증이 필요한 접근입니다"))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/option-values/1"));
    }

    @Test
    @DisplayName("옵션 값 변경 요청 검증")
    @WithCustomMockUser
    void updateOptionValue_validation() throws Exception {
        //given
        OptionUpdateRequest request = createOptionUpdateRequest().name(null).build();
        //when
        //then
        mockMvc.perform(patch("/option-values/{optionValueId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value("VALIDATION"))
                .andExpect(jsonPath("message").value("이름은 필수입니다"))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/option-values/1"));
    }

    @Test
    @DisplayName("옵션 값을 삭제한다")
    @WithCustomMockUser
    void deleteOptionValue() throws Exception {
        //given
        willDoNothing().given(optionService).deleteOptionValue(anyLong());
        //when
        //then
        mockMvc.perform(delete("/option-values/{optionValueId}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("옵션 값을 삭제하려면 관리자 권한이여야 한다")
    @WithCustomMockUser(userRole = UserRole.ROLE_USER)
    void deleteOptionValue_user_role() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(delete("/option-values/{optionValueId}", 1L))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("FORBIDDEN"))
                .andExpect(jsonPath("message").value("요청 권한이 부족합니다"))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/option-values/1"));
    }

    @Test
    @DisplayName("로그인 하지 않은 사용자는 옵션 값을 삭제할 수 없다")
    void deleteOptionValue_unAuthorized() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(delete("/option-values/{optionValueId}", 1L))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("message").value("인증이 필요한 접근입니다"))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/option-values/1"));
    }

    private static OptionCreateRequest.OptionCreateRequestBuilder createOptionRequest() {
        return OptionCreateRequest.builder().name("사이즈").values(
                List.of("XL", "L", "M", "S")
        );
    }

    private static OptionUpdateRequest.OptionUpdateRequestBuilder createOptionUpdateRequest(){
        return OptionUpdateRequest.builder().name("새 이름");
    }

    private static OptionResponse.OptionResponseBuilder createOptionResponse() {
        return OptionResponse.builder()
                .id(1L)
                .name("사이즈")
                .values(
                        List.of(
                                OptionValueResponse.builder().id(1L).name("XL").build(),
                                OptionValueResponse.builder().id(2L).name("L").build(),
                                OptionValueResponse.builder().id(3L).name("M").build(),
                                OptionValueResponse.builder().id(4L).name("S").build()
                        ));
    }

    private static OptionValueResponse.OptionValueResponseBuilder createOptionValueResponse() {
        return OptionValueResponse.builder()
                .id(1L)
                .name("새 이름");
    }

    private static Stream<Arguments> provideInvalidRequest() {
        return Stream.of(
                Arguments.of("name 이 null", createOptionRequest().name(null).build(), "옵션 이름은 필수 입니다"),
                Arguments.of("value 가 비어있음", createOptionRequest().values(List.of()).build(), "최소 1개의 옵션 값을 입력해야합니다"),
                Arguments.of("중복된 value", createOptionRequest().values(List.of("중복", "중복")).build(), "옵션값은 중복될 수 없습니다")
        );
    }
}
