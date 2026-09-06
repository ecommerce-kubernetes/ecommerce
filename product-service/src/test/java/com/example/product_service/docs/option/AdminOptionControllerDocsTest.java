package com.example.product_service.docs.option;

import com.example.product_service.docs.RestDocsSupport;
import com.example.product_service.option.adapter.in.web.AdminOptionController;
import com.example.product_service.option.adapter.in.web.dto.request.CreateOptionTypeRequest;
import com.example.product_service.option.adapter.in.web.dto.request.UpdateOptionTypeRequest;
import com.example.product_service.option.adapter.in.web.dto.request.UpdateOptionValueRequest;
import com.example.product_service.option.application.service.OptionCommandService;
import com.example.product_service.option.application.service.OptionQueryService;
import com.example.product_service.option.application.service.dto.command.CreateOptionTypeCommand;
import com.example.product_service.option.application.service.dto.result.OptionTypeResult;
import com.example.product_service.option.application.service.dto.result.OptionTypesResult;
import com.example.product_service.option.fixture.OptionResultFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static com.example.product_service.docs.descriptor.OptionDescriptor.*;
import static com.example.product_service.option.fixture.OptionRequestFixture.anCreateOptionTypeRequest;
import static com.example.product_service.option.fixture.OptionRequestFixture.anUpdateOptionTypeRequest;
import static com.example.product_service.option.fixture.OptionResultFixture.anOptionTypeResult;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
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
                        responseFields(optionTypeResponse())
                ));
    }

    @Test
    @DisplayName("옵션을 수정한다")
    void updateOptionType() throws Exception {
        //given
        UpdateOptionTypeRequest request = anUpdateOptionTypeRequest().build();
        HttpHeaders authHeader = createAuthHeader("ROLE_ADMIN");
        //when
        //then
        mockMvc.perform(patch("/admin/option-types/{optionTypeId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(authHeader)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "options/update",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(AUTH_HEADER),
                        requestFields(updateOptionTypeRequest()),
                        responseFields(updateOptionTypeResponse())
                ));
    }

    @Test
    @DisplayName("옵션을 삭제한다")
    void deleteOption() throws Exception {
        //given
        HttpHeaders authHeader = createAuthHeader("ROLE_ADMIN");
        //when
        //then
        mockMvc.perform(delete("/options/{optionTypeId}", 1L)
                        .headers(authHeader))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(document(
                        "options/delete",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(AUTH_HEADER)
                ));
    }

    @Test
    @DisplayName("옵션 값 수정")
    void updateOptionValue() throws Exception {
        //given
        UpdateOptionValueRequest request = UpdateOptionValueRequest.builder()
                .name("새 이름")
                .build();
        HttpHeaders authHeader = createAuthHeader("ROLE_ADMIN");
        //when
        //then
        mockMvc.perform(patch("/option-values/{optionValueId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(authHeader)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "option-values/update",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(AUTH_HEADER),
                        responseFields(getOptionValueUpdateResponse())
                ));
    }

    @Test
    @DisplayName("옵션 값 삭제")
    void deleteOptionValue() throws Exception {
        //given
        HttpHeaders authHeader = createAuthHeader("ROLE_ADMIN");
        //when
        //then
        mockMvc.perform(delete("/option-values/{optionValueId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(authHeader))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(document(
                        "option-values/delete",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(AUTH_HEADER)
                ));
    }
}
