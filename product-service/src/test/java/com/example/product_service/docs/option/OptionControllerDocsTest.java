package com.example.product_service.docs.option;

import com.example.product_service.option.adapter.in.web.OptionController;
import com.example.product_service.option.adapter.in.web.dto.request.CreateOptionRequest;
import com.example.product_service.option.adapter.in.web.dto.request.OptionValueRequest;
import com.example.product_service.option.adapter.in.web.dto.request.UpdateOptionTypeRequest;
import com.example.product_service.option.adapter.in.web.dto.request.UpdateOptionValueRequest;
import com.example.product_service.option.application.service.OptionService;
import com.example.product_service.option.application.service.dto.command.OptionCommand;
import com.example.product_service.option.application.service.dto.result.OptionResult;
import com.example.product_service.option.application.service.dto.result.OptionValueResult;
import com.example.product_service.docs.RestDocsSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;

import static com.example.product_service.docs.descriptor.OptionDescriptor.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OptionControllerDocsTest extends RestDocsSupport {
    OptionService optionService = Mockito.mock(OptionService.class);

    @Override
    protected Object initController() {
        return new OptionController(optionService);
    }

    @Test
    @DisplayName("옵션을 저장한다")
    void saveOption() throws Exception {
        //given
        CreateOptionRequest request = CreateOptionRequest.builder()
                .name("사이즈")
                .values(
                        List.of(OptionValueRequest.builder()
                                .name("XL").build())
                ).build();
        OptionResult result = createOptionResponse().build();
        HttpHeaders authHeader = createAuthHeader("ROLE_ADMIN");
        given(optionService.saveOption(any(OptionCommand.Create.class)))
                .willReturn(result);
        //when
        //then
        mockMvc.perform(post("/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .headers(authHeader))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(document(
                        "options",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestFields(getCreateRequest()),
                        requestHeaders(AUTH_HEADER),
                        responseFields(getOptionResponse())
                ));
    }

    @Test
    @DisplayName("옵션을 조회한다")
    void getOption() throws Exception {
        //given
        OptionResult result = createOptionResponse().build();
        given(optionService.getOption(anyLong()))
                .willReturn(result);
        //when
        //then
        mockMvc.perform(get("/options/{optionTypeId}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "options/get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(getOptionResponse())
                ));
    }

    @Test
    @DisplayName("옵션 목록을 조회한다")
    void getOptions() throws Exception {
        //given
        OptionResult result = createOptionResponse().build();
        given(optionService.getOptions())
                .willReturn(List.of(result));
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
        given(optionService.updateOptionTypeName(any(OptionCommand.UpdateOptionType.class)))
                .willReturn(result);
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
                        responseFields(getOptionResponse())
                ));
    }

    @Test
    @DisplayName("옵션을 삭제한다")
    void deleteOption() throws Exception {
        //given
        willDoNothing().given(optionService).deleteOption(anyLong());
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
        given(optionService.updateOptionValueName(any(OptionCommand.UpdateOptionValue.class)))
                .willReturn(result);
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
        willDoNothing().given(optionService).deleteOptionValue(anyLong());
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
