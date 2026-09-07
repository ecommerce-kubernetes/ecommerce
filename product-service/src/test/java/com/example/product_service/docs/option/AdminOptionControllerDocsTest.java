package com.example.product_service.docs.option;

import com.example.product_service.docs.RestDocsSupport;
import com.example.product_service.option.adapter.in.web.AdminOptionController;
import com.example.product_service.option.adapter.in.web.dto.request.AddOptionValueRequest;
import com.example.product_service.option.adapter.in.web.dto.request.CreateOptionTypeRequest;
import com.example.product_service.option.adapter.in.web.dto.request.UpdateOptionTypeRequest;
import com.example.product_service.option.adapter.in.web.dto.request.UpdateOptionValueRequest;
import com.example.product_service.option.application.service.OptionCommandService;
import com.example.product_service.option.application.service.OptionQueryService;
import com.example.product_service.option.application.service.dto.command.AddOptionValueCommand;
import com.example.product_service.option.application.service.dto.command.CreateOptionTypeCommand;
import com.example.product_service.option.application.service.dto.command.UpdateOptionTypeCommand;
import com.example.product_service.option.application.service.dto.command.UpdateOptionValueCommand;
import com.example.product_service.option.application.service.dto.result.OptionTypeResult;
import com.example.product_service.option.application.service.dto.result.OptionTypesResult;
import com.example.product_service.option.fixture.OptionResultFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static com.example.product_service.docs.descriptor.OptionDescriptor.*;
import static com.example.product_service.option.fixture.OptionRequestFixture.*;
import static com.example.product_service.option.fixture.OptionResultFixture.anOptionTypeResult;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminOptionControllerDocsTest extends RestDocsSupport {

    private OptionCommandService optionCommandService = Mockito.mock(OptionCommandService.class);

    private OptionQueryService optionQueryService = Mockito.mock(OptionQueryService.class);

    @Override
    protected Object initController() {
        return new AdminOptionController(optionCommandService, optionQueryService);
    }

    @Test
    @DisplayName("옵션을 저장한다")
    void createOptionType() throws Exception {
        //given
        CreateOptionTypeRequest request = anCreateOptionTypeRequest().build();
        Long optionTypeId = 1L;
        given(optionCommandService.createOptionType(any(CreateOptionTypeCommand.class))).willReturn(optionTypeId);
        HttpHeaders authHeader = createAuthHeader("ROLE_ADMIN");
        //when
        //then
        mockMvc.perform(post("/admin/option-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .headers(authHeader))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(document(
                        "admin/option-types/create",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(AUTH_HEADER),
                        requestFields(createOptionTypeRequest()),
                        responseFields(createOptionTypeResponse())
                ));
    }

    @Test
    @DisplayName("옵션 타입 목록을 조회한다")
    void getOptionTypes() throws Exception {
        //given
        OptionTypesResult optionTypes = OptionResultFixture.anOptionTypesResult().build();
        HttpHeaders authHeader = createAuthHeader("ROLE_ADMIN");
        given(optionQueryService.getTypes()).willReturn(optionTypes);
        //when
        //then
        mockMvc.perform(get("/admin/option-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(authHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "admin/option-types/list",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(AUTH_HEADER),
                        responseFields(optionTypesResponse())
                ));
    }

    @Test
    @DisplayName("옵션 타입을 조회한다")
    void getOptionType() throws Exception {
        //given
        Long optionTypeId = 1L;
        OptionTypeResult type = anOptionTypeResult().build();
        HttpHeaders authHeader = createAuthHeader("ROLE_ADMIN");
        given(optionQueryService.getType(anyLong())).willReturn(type);
        //when
        //then
        mockMvc.perform(get("/admin/option-types/{optionTypeId}", optionTypeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(authHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "admin/option-types/detail",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(AUTH_HEADER),
                        pathParameters(parameterWithName("optionTypeId").description("조회할 옵션 타입 ID")),
                        responseFields(optionTypeResponse())
                ));
    }

    @Test
    @DisplayName("옵션 타입을 수정한다")
    void updateOptionType() throws Exception {
        //given
        Long optionTypeId = 1L;
        UpdateOptionTypeRequest request = anUpdateOptionTypeRequest().build();
        HttpHeaders authHeader = createAuthHeader("ROLE_ADMIN");
        given(optionCommandService.updateOptionType(any(UpdateOptionTypeCommand.class))).willReturn(optionTypeId);
        //when
        //then
        mockMvc.perform(patch("/admin/option-types/{optionTypeId}", optionTypeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(authHeader)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "admin/option-types/update",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(AUTH_HEADER),
                        requestFields(updateOptionTypeRequest()),
                        pathParameters(parameterWithName("optionTypeId").description("수정할 옵션 타입 ID")),
                        responseFields(updateOptionTypeResponse())
                ));
    }

    @Test
    @DisplayName("옵션 타입을 삭제한다")
    void deleteOptionType() throws Exception {
        //given
        Long optionTypeId = 1L;
        HttpHeaders authHeader = createAuthHeader("ROLE_ADMIN");
        willDoNothing().given(optionCommandService).deleteOptionType(anyLong());
        //when
        //then
        mockMvc.perform(delete("/admin/option-types/{optionTypeId}", optionTypeId)
                        .headers(authHeader))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(document(
                        "admin/option-types/delete",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        pathParameters(parameterWithName("optionTypeId").description("삭제할 옵션 타입 ID")),
                        requestHeaders(AUTH_HEADER)
                ));
    }

    @Test
    @DisplayName("옵션 값을 추가한다")
    void addOptionValue() throws Exception {
        //given
        Long optionTypeId = 1L;
        Long optionValueId = 10L;
        AddOptionValueRequest request = anAddOptionValueRequest().build();
        HttpHeaders authHeader = createAuthHeader("ROLE_ADMIN");
        given(optionCommandService.addOptionValue(any(AddOptionValueCommand.class))).willReturn(optionValueId);
        //when
        //then
        mockMvc.perform(post("/admin/option-types/{optionTypeId}/values", optionTypeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(authHeader)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(document(
                        "admin/option-types/add-value",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(AUTH_HEADER),
                        pathParameters(parameterWithName("optionTypeId").description("옵션 값을 추가할 옵션 타입 ID")),
                        requestFields(addOptionValueRequest()),
                        responseFields(addOptionValueResponse())
                ));
    }

    @Test
    @DisplayName("옵션 값을 수정한다")
    void updateOptionValue() throws Exception {
        //given
        Long optionTypeId = 1L;
        Long optionValueId = 10L;
        UpdateOptionValueRequest request = anUpdateOptionValueRequest().build();
        HttpHeaders authHeader = createAuthHeader("ROLE_ADMIN");
        given(optionCommandService.updateOptionValue(any(UpdateOptionValueCommand.class))).willReturn(optionValueId);
        //when
        //then
        mockMvc.perform(patch("/admin/option-types/{optionTypeId}/values/{optionValueId}", optionTypeId, optionValueId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(authHeader)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "admin/option-types/update-value",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(AUTH_HEADER),
                        pathParameters(parameterWithName("optionTypeId").description("옵션 값을 수정할 옵션 타입 ID"),
                                parameterWithName("optionValueId").description("수정할 옵션 값 ID")),
                        requestFields(updateOptionValueRequest()),
                        responseFields(updateOptionValueResponse())
                ));
    }

    @Test
    @DisplayName("옵션 값을 삭제한다")
    void deleteOptionValue() throws Exception {
        //given
        Long optionTypeId = 1L;
        Long optionValueId = 10L;
        HttpHeaders authHeader = createAuthHeader("ROLE_ADMIN");
        willDoNothing().given(optionCommandService).deleteOptionValue(anyLong(), anyLong());
        //when
        //then
        mockMvc.perform(delete("/admin/option-types/{optionTypeId}/values/{optionValueId}", optionTypeId, optionValueId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(authHeader))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(document(
                        "admin/option-types/delete-value",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(AUTH_HEADER),
                        pathParameters(parameterWithName("optionTypeId").description("옵션 값을 삭제할 옵션 타입 ID"),
                                parameterWithName("optionValueId").description("삭제할 옵션 값 ID"))
                ));
    }
}
