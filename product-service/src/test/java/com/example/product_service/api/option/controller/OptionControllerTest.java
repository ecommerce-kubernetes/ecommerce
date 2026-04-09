package com.example.product_service.api.option.controller;

import com.example.product_service.api.common.security.model.UserRole;
import com.example.product_service.api.option.controller.dto.request.OptionRequest;
import com.example.product_service.api.option.controller.dto.response.OptionResponse;
import com.example.product_service.api.option.service.dto.command.OptionCommand;
import com.example.product_service.api.option.service.dto.result.OptionResult;
import com.example.product_service.api.option.service.dto.result.OptionValueResult;
import com.example.product_service.support.ControllerTestSupport;
import com.example.product_service.support.security.annotation.WithCustomMockUser;
import com.example.product_service.support.security.config.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestSecurityConfig.class)
class OptionControllerTest extends ControllerTestSupport {

    @Nested
    @DisplayName("옵션 저장")
    class SaveOption {
        @Test
        @DisplayName("옵션을 저장한다")
        @WithCustomMockUser
        void saveOption() throws Exception {
            //given
            OptionRequest.Create request = fixtureMonkey.giveMeOne(OptionRequest.Create.class);
            OptionResult result = fixtureMonkey.giveMeOne(OptionResult.class);
            assert result != null;
            OptionResponse.Detail response = OptionResponse.Detail.from(result);
            given(optionService.saveOption(any(OptionCommand.Create.class)))
                    .willReturn(result);
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
            OptionRequest.Create request = fixtureMonkey.giveMeOne(OptionRequest.Create.class);
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
            OptionRequest.Create request = fixtureMonkey.giveMeOne(OptionRequest.Create.class);
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
        void saveOption_validation(String description, OptionRequest.Create request, String message) throws Exception {
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

        private static Stream<Arguments> provideInvalidRequest() {
            return Stream.of(
                    Arguments.of("name 이 null", OptionRequest.Create.builder()
                            .name(null)
                            .values(List.of(OptionRequest.Value.builder().name("XL").build())).build(), "옵션 이름은 필수 입니다"),
                    Arguments.of("value 가 비어있음", OptionRequest.Create.builder()
                            .name("옵션").values(List.of()).build(), "최소 1개의 옵션 값을 입력해야합니다"),
                    Arguments.of("중복된 value", OptionRequest.Create.builder()
                                    .name("옵션")
                                    .values(
                                            List.of(OptionRequest.Value.builder().name("XL").build(),
                                                    OptionRequest.Value.builder().name("XL").build())
                                    ).build(),
                            "옵션값은 중복될 수 없습니다")
            );
        }
    }

    @Nested
    @DisplayName("옵션 단일 조회")
    class GetOption {
        @Test
        @DisplayName("옵션을 조회한다")
        void getOption() throws Exception {
            //given
            OptionResult response = fixtureMonkey.giveMeOne(OptionResult.class);
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
    }

    @Nested
    @DisplayName("옵션 목록 조회")
    class GetOptions {
        @Test
        @DisplayName("옵션 목록을 조회한다")
        void getOptions() throws Exception {
            //given
            List<OptionResult> results =
                    fixtureMonkey.giveMe(OptionResult.class, 3);
            given(optionService.getOptions())
                    .willReturn(results);
            List<OptionResponse.Detail> responses = OptionResponse.Detail.from(results);
            //when
            //then
            mockMvc.perform(get("/options")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(responses)));
        }
    }

    @Nested
    @DisplayName("옵션 타입 수정")
    class UpdateOptionType {
        @Test
        @DisplayName("옵션을 수정한다")
        @WithCustomMockUser
        void updateOptionType() throws Exception {
            //given
            OptionRequest.UpdateOptionType request = fixtureMonkey.giveMeOne(OptionRequest.UpdateOptionType.class);
            OptionResult result = fixtureMonkey.giveMeOne(OptionResult.class);
            assert result != null;
            OptionResponse.Detail response = OptionResponse.Detail.from(result);
            given(optionService.updateOptionTypeName(any(OptionCommand.UpdateOptionType.class)))
                    .willReturn(result);
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
            OptionRequest.UpdateOptionType request = fixtureMonkey.giveMeOne(OptionRequest.UpdateOptionType.class);
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
            OptionRequest.UpdateOptionType request = fixtureMonkey.giveMeOne(OptionRequest.UpdateOptionType.class);
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
            OptionRequest.UpdateOptionType request = OptionRequest.UpdateOptionType.builder()
                    .name(null)
                    .build();
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
    }

    @Nested
    @DisplayName("옵션 삭제")
    class DeleteOption {
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
    }

    @Test
    @DisplayName("옵션 값을 수정한다")
    @WithCustomMockUser
    void updateOptionValue() throws Exception {
        //given
        OptionRequest.UpdateOptionValue request = fixtureMonkey.giveMeOne(OptionRequest.UpdateOptionValue.class);
        OptionValueResult response = fixtureMonkey.giveMeOne(OptionValueResult.class);
        given(optionService.updateOptionValueName(any(OptionCommand.UpdateOptionValue.class)))
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
        OptionRequest.UpdateOptionValue request = fixtureMonkey.giveMeOne(OptionRequest.UpdateOptionValue.class);
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
        OptionRequest.UpdateOptionValue request = fixtureMonkey.giveMeOne(OptionRequest.UpdateOptionValue.class);
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
        OptionRequest.UpdateOptionValue request = OptionRequest.UpdateOptionValue.builder()
                .name(null)
                .build();
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

    @Nested
    @DisplayName("옵션 값 삭제")
    class DeleteOptionValue {
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

    }
}
