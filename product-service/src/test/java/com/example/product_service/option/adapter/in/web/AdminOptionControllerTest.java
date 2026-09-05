package com.example.product_service.option.adapter.in.web;

import com.example.product_service.option.adapter.in.web.dto.request.CreateOptionRequest;
import com.example.product_service.option.adapter.in.web.dto.request.UpdateOptionTypeRequest;
import com.example.product_service.option.adapter.in.web.dto.request.UpdateOptionValueRequest;
import com.example.product_service.option.adapter.in.web.dto.response.OptionDetailResponse;
import com.example.product_service.option.application.service.dto.command.OptionCommand;
import com.example.product_service.option.application.service.dto.result.OptionResult;
import com.example.product_service.option.application.service.dto.result.OptionValueResult;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestSecurityConfig.class)
class AdminOptionControllerTest extends ControllerTestSupport {

    @Test
    @DisplayName("옵션을 저장한다")
    @WithCustomMockUser
    void createOptionType() throws Exception {
        //given
        CreateOptionRequest request = fixtureMonkey.giveMeOne(CreateOptionRequest.class);
        OptionResult result = fixtureMonkey.giveMeOne(OptionResult.class);
        assert result != null;
        OptionDetailResponse response = OptionDetailResponse.from(result);
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

    @ParameterizedTest(name = "{0}")
    @DisplayName("옵션 저장 요청 검증")
    @MethodSource("provideInvalidRequest")
    @WithCustomMockUser
    void saveOption_Types_validation(String description, CreateOptionRequest request, String message) throws Exception {
        //given
        //when
        //then
        mockMvc.perform(post("/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("code").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("errors[0].reason").value(message))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/options"));
    }

    private static Stream<Arguments> provideInvalidRequest() {
        return null;
    }

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

    @Test
    @DisplayName("옵션 목록을 조회한다")
    void getOptions() throws Exception {
        //given
        List<OptionResult> results =
                fixtureMonkey.giveMe(OptionResult.class, 3);
        given(optionService.getOptions())
                .willReturn(results);
        List<OptionDetailResponse> responses = OptionDetailResponse.from(results);
        //when
        //then
        mockMvc.perform(get("/options")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(responses)));
    }
    @Test
    @DisplayName("옵션을 수정한다")
    @WithCustomMockUser
    void updateOptionType() throws Exception {
        //given
        UpdateOptionTypeRequest request = fixtureMonkey.giveMeOne(UpdateOptionTypeRequest.class);
        OptionResult result = fixtureMonkey.giveMeOne(OptionResult.class);
        assert result != null;
        OptionDetailResponse response = OptionDetailResponse.from(result);
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
    @DisplayName("옵션 수정 요청 검증")
    @WithCustomMockUser
    void updateOption_Type_validation() throws Exception {
        //given
        UpdateOptionTypeRequest request = UpdateOptionTypeRequest.builder()
                .name(null)
                .build();
        //when
        //then
        mockMvc.perform(patch("/options/{optionTypeId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("code").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("errors[0].reason").value("이름은 필수입니다"))
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
    @DisplayName("옵션 값을 수정한다")
    @WithCustomMockUser
    void updateOptionValue() throws Exception {
        //given
        UpdateOptionValueRequest request = fixtureMonkey.giveMeOne(UpdateOptionValueRequest.class);
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
    @DisplayName("옵션 값 변경 요청 검증")
    @WithCustomMockUser
    void updateOptionValue_validation() throws Exception {
        //given
        UpdateOptionValueRequest request = UpdateOptionValueRequest.builder()
                .name(null)
                .build();
        //when
        //then
        mockMvc.perform(patch("/option-values/{optionValueId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("errors[0].reason").value("이름은 필수입니다"))
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
}
