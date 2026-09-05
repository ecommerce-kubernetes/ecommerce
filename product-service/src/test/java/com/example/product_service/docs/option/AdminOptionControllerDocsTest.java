package com.example.product_service.docs.option;

import com.example.product_service.docs.RestDocsSupport;
import com.example.product_service.option.adapter.in.web.AdminOptionController;
import com.example.product_service.option.adapter.in.web.dto.request.CreateOptionTypeRequest;
import com.example.product_service.option.adapter.in.web.dto.request.UpdateOptionTypeRequest;
import com.example.product_service.option.adapter.in.web.dto.request.UpdateOptionValueRequest;
import com.example.product_service.option.application.service.OptionCommandService;
import com.example.product_service.option.application.service.OptionQueryService;
import com.example.product_service.option.application.service.dto.command.CreateOptionTypeCommand;
import com.example.product_service.option.application.service.dto.result.OptionResult;
import com.example.product_service.option.application.service.dto.result.OptionValueResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;

import static com.example.product_service.docs.descriptor.OptionDescriptor.*;
import static com.example.product_service.option.fixture.OptionRequestFixture.anCreateOptionTypeRequest;
import static org.mockito.ArgumentMatchers.any;
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
    @DisplayName("옵션을 조회한다")
    void getOption() throws Exception {
        //given
        OptionResult result = createOptionResponse().build();
        //when
        //then
        mockMvc.perform(get("/options/{optionTypeId}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "options/get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(createOptionTypeResponse())
                ));
    }

    @Test
    @DisplayName("옵션 목록을 조회한다")
    void getOptions() throws Exception {
        //given
        OptionResult result = createOptionResponse().build();
        //when
        //then
        mockMvc.perform(get("/options"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "options/list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(getOptionListResponse())
                ));
    }

    @Test
    @DisplayName("옵션을 수정한다")
    void updateOptionType() throws Exception {
        //given
        UpdateOptionTypeRequest request = UpdateOptionTypeRequest.builder()
                .name("새 이름")
                .build();
        OptionResult result = createOptionResponse().name("새 이름").build();
        HttpHeaders authHeader = createAuthHeader("ROLE_ADMIN");
        //when
        //then
        mockMvc.perform(patch("/options/{optionTypeId}", 1L)
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
                        requestFields(getOptionUpdateRequest()),
                        requestHeaders(AUTH_HEADER),
                        responseFields(createOptionTypeResponse())
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
        OptionValueResult result = createOptionValueResponse().name("새 이름").build();
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
                        requestFields(getOptionUpdateRequest()),
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

    private OptionResult.OptionResultBuilder createOptionResponse() {
        return OptionResult.builder()
                .id(1L)
                .name("사이즈")
                .values(
                        List.of(
                                OptionValueResult.builder().id(1L).name("XL").build(),
                                OptionValueResult.builder().id(2L).name("L").build(),
                                OptionValueResult.builder().id(3L).name("M").build(),
                                OptionValueResult.builder().id(4L).name("S").build()
                        ));
    }

    private OptionValueResult.OptionValueResultBuilder createOptionValueResponse() {
        return OptionValueResult.builder()
                .id(1L)
                .name("XL");
    }
}
