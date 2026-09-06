package com.example.product_service.option.adapter.in.web;

import com.example.product_service.option.adapter.in.web.dto.request.CreateOptionTypeRequest;
import com.example.product_service.option.adapter.in.web.dto.request.UpdateOptionTypeRequest;
import com.example.product_service.option.adapter.in.web.dto.request.UpdateOptionValueRequest;
import com.example.product_service.option.application.service.OptionCommandService;
import com.example.product_service.option.application.service.OptionQueryService;
import com.example.product_service.option.application.service.dto.command.CreateOptionTypeCommand;
import com.example.product_service.option.application.service.dto.command.UpdateOptionTypeCommand;
import com.example.product_service.option.application.service.dto.result.OptionTypeResult;
import com.example.product_service.option.application.service.dto.result.OptionTypesResult;
import com.example.product_service.support.security.annotation.WithCustomMockUser;
import com.example.product_service.support.security.config.TestSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.example.product_service.option.fixture.OptionRequestFixture.anCreateOptionTypeRequest;
import static com.example.product_service.option.fixture.OptionRequestFixture.anUpdateOptionTypeRequest;
import static com.example.product_service.option.fixture.OptionResultFixture.anOptionTypeResult;
import static com.example.product_service.option.fixture.OptionResultFixture.anOptionTypesResult;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestSecurityConfig.class)
@WebMvcTest(controllers = AdminOptionController.class)
class AdminOptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OptionCommandService optionCommandService;

    @MockitoBean
    private OptionQueryService optionQueryService;

    @Test
    @DisplayName("옵션을 저장한다")
    @WithCustomMockUser
    void createOptionType() throws Exception {
        //given
        CreateOptionTypeRequest request = anCreateOptionTypeRequest().build();
        Long optionTypeId = 1L;
        given(optionCommandService.createOptionType(any(CreateOptionTypeCommand.class))).willReturn(optionTypeId);
        //when
        //then
        mockMvc.perform(post("/admin/option-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(optionTypeId));
    }

    @ParameterizedTest(name = "{0}")
    @DisplayName("옵션 저장 요청 검증")
    @MethodSource("provideInvalidCreateOptionTypeRequest")
    @WithCustomMockUser
    void saveOption_Types_validation(String description, CreateOptionTypeRequest request, String expectedField, String expectedMessage) throws Exception {
        //given
        //when
        //then
        mockMvc.perform(post("/admin/option-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("code").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("message").value("입력값이 올바르지 않습니다."))
                .andExpect(jsonPath("errors[0].field").value(expectedField))
                .andExpect(jsonPath("errors[0].reason").value(expectedMessage))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/admin/option-types"));
    }

    @Test
    @DisplayName("옵션 타입 목록을 조회한다")
    void getOptionTypes() throws Exception {
        //given
        OptionTypesResult types = anOptionTypesResult().build();
        given(optionQueryService.getTypes()).willReturn(types);

        OptionTypeResult type = types.types().getFirst();
        OptionTypeResult.OptionValueResult value = type.values().getFirst();
        //when
        //then
        mockMvc.perform(get("/admin/option-types")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.optionTypes").isArray())
                .andExpect(jsonPath("$.optionTypes[0].id").value(type.id()))
                .andExpect(jsonPath("$.optionTypes[0].name").value(type.name()))
                .andExpect(jsonPath("$.optionTypes[0].values").isArray())
                .andExpect(jsonPath("$.optionTypes[0].values[0].id").value(value.id()))
                .andExpect(jsonPath("$.optionTypes[0].values[0].name").value(value.name()));
    }

    @Test
    @DisplayName("옵션 타입을 조회한다")
    void getOptionType() throws Exception {
        //given
        Long optionTypeId = 1L;
        OptionTypeResult type = anOptionTypeResult().build();
        given(optionQueryService.getType(optionTypeId)).willReturn(type);
        OptionTypeResult.OptionValueResult value = type.values().getFirst();
        //when
        //then
        mockMvc.perform(get("/admin/option-types/{optionTypeId}", optionTypeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(type.id()))
                .andExpect(jsonPath("$.name").value(type.name()))
                .andExpect(jsonPath("$.values").isArray())
                .andExpect(jsonPath("$.values[0].id").value(value.id()))
                .andExpect(jsonPath("$.values[0].name").value(value.name()));
    }

    @Test
    @DisplayName("옵션 타입을 수정한다")
    @WithCustomMockUser
    void updateOptionType() throws Exception {
        //given
        Long optionTypeId = 1L;
        UpdateOptionTypeRequest request = anUpdateOptionTypeRequest().build();
        given(optionCommandService.updateOptionType(any(UpdateOptionTypeCommand.class))).willReturn(optionTypeId);
        //when
        //then
        mockMvc.perform(patch("/admin/option-types/{optionTypeId}", optionTypeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(optionTypeId));
    }

    @ParameterizedTest(name = "{0}")
    @DisplayName("옵션 타입 수정 요청 검증")
    @MethodSource("provideInvalidUpdateTypeRequest")
    @WithCustomMockUser
    void updateOption_Type_validation(String description, UpdateOptionTypeRequest request, String expectedField, String expectedMessage) throws Exception {
        //given
        Long optionTypeId = 1L;
        //when
        //then
        mockMvc.perform(patch("/admin/option-types/{optionTypeId}", optionTypeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("code").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("message").value("입력값이 올바르지 않습니다."))
                .andExpect(jsonPath("errors[0].field").value(expectedField))
                .andExpect(jsonPath("errors[0].reason").value(expectedMessage))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/admin/option-types/" + optionTypeId));
    }

    @Test
    @DisplayName("옵션 타입을 삭제한다")
    @WithCustomMockUser
    void deleteOptionType() throws Exception {
        //given
        Long optionTypeId = 1L;
        willDoNothing().given(optionCommandService).deleteOptionType(anyLong());
        //when
        //then
        mockMvc.perform(delete("/admin/option-types/{optionTypeId}", optionTypeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("옵션 값을 수정한다")
    @WithCustomMockUser
    void updateOptionValue() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(patch("/option-values/{optionValueId}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
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
        //when
        //then
        mockMvc.perform(delete("/option-values/{optionValueId}", 1L))
                .andExpect(status().isNoContent());
    }

    private static Stream<Arguments> provideInvalidCreateOptionTypeRequest() {
        return Stream.of(
                Arguments.of(
                        "옵션 타입 이름이 누락되면 예외가 발생한다",
                        anCreateOptionTypeRequest().name(null).build(),
                        "name",
                        "옵션 타입 이름은 필수 입니다"
                ),
                Arguments.of(
                        "옵션 값이 누락되면 예외가 발생한다",
                        anCreateOptionTypeRequest().values(null).build(),
                        "values",
                        "최소 1개의 옵션 값을 입력해야합니다"
                ),
                Arguments.of(
                        "옵션 값이 빈 리스트면 예외가 발생한다",
                        anCreateOptionTypeRequest().values(Collections.emptyList()).build(),
                        "values",
                        "최소 1개의 옵션 값을 입력해야합니다"
                ),
                Arguments.of(
                        "옵션 값 이름이 누락되면 예외가 발생한다",
                        anCreateOptionTypeRequest()
                                .values(
                                        List.of(CreateOptionTypeRequest.CreateOptionValueRequest.builder().name(null).build())
                                ).build(),
                        "values[0].name",
                        "옵션 값 이름은 필수 입니다"
                ),
                Arguments.of(
                        "옵션 값 이름이 중복되면 예외가 발생한다",
                        anCreateOptionTypeRequest()
                                .values(
                                        List.of(
                                                CreateOptionTypeRequest.CreateOptionValueRequest.builder().name("중복").build(),
                                                CreateOptionTypeRequest.CreateOptionValueRequest.builder().name("중복").build()
                                        )
                                ).build(),
                        "values",
                        "옵션 값은 중복될 수 없습니다"
                )
        );
    }

    public static Stream<Arguments> provideInvalidUpdateTypeRequest() {
        return Stream.of(
                Arguments.of(
                        "옵션 타입 이름이 누락되면 예외가 발생한다",
                        anUpdateOptionTypeRequest().name(null).build(),
                        "name",
                        "옵션 타입 이름은 필수 입니다"
                )
        );
    }
}
