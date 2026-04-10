package com.example.product_service.docs.option;

import com.example.product_service.api.option.controller.OptionController;
import com.example.product_service.api.option.controller.dto.request.OptionRequest;
import com.example.product_service.api.option.controller.dto.response.OptionResponse;
import com.example.product_service.api.option.service.OptionService;
import com.example.product_service.api.option.service.dto.command.OptionCommand;
import com.example.product_service.api.option.service.dto.result.OptionResult;
import com.example.product_service.api.option.service.dto.result.OptionValueResult;
import com.example.product_service.docs.RestDocsSupport;
import com.example.product_service.docs.descriptor.OptionDescriptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OptionControllerDocsTest extends RestDocsSupport {
    OptionService optionService = Mockito.mock(OptionService.class);
    @Override
    protected String getTag() {
        return "Option";
    }

    @Override
    protected Object initController() {
        return new OptionController(optionService);
    }

    @Test
    @DisplayName("옵션을 저장한다")
    void saveOption() throws Exception {
        //given
        OptionRequest.Create request = OptionRequest.Create.builder()
                .name("사이즈")
                .values(
                        List.of(OptionRequest.Value.builder()
                                .name("XL").build())
                ).build();
        OptionResult result = createOptionResponse().build();
        OptionResponse.Detail response = OptionResponse.Detail.from(result);
        HttpHeaders adminHeader = createAdminHeader();
        given(optionService.saveOption(any(OptionCommand.Create.class)))
                .willReturn(result);
        //when
        //then
        mockMvc.perform(post("/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .headers(adminHeader))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(createSecuredDocument(
                        "02-option-01-create",
                        "옵션 생성",
                        "새로운 옵션과 값을 생성한다",
                        OptionDescriptor.getCreateRequest(),
                        OptionDescriptor.getOptionResponse())
                );
    }

    @Test
    @DisplayName("옵션을 조회한다")
    void getOption() throws Exception {
        //given
        OptionResult result = createOptionResponse().build();
        OptionResponse.Detail response = OptionResponse.Detail.from(result);
        given(optionService.getOption(anyLong()))
                .willReturn(result);
        //when
        //then
        mockMvc.perform(get("/options/{optionTypeId}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(createPublicDocument(
                        "02-option-02-get",
                        "옵션 조회",
                        "옵션을 조회한다",
                        OptionDescriptor.getOptionResponse(),
                        parameterWithName("optionTypeId").description("조회할 옵션 타입 ID"))
                );
    }

    @Test
    @DisplayName("옵션 목록을 조회한다")
    void getOptions() throws Exception {
        //given
        OptionResult result = createOptionResponse().build();
        given(optionService.getOptions())
                .willReturn(List.of(result));
        OptionResponse.Detail response = OptionResponse.Detail.from(result);
        //when
        //then
        mockMvc.perform(get("/options"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(response))))
                .andDo(createPublicDocument(
                        "02-option-03-get-list",
                        "옵션 목록 조회",
                        "옵션 목록을 조회한다",
                        OptionDescriptor.getOptionListResponse())
                );
    }

    @Test
    @DisplayName("옵션을 수정한다")
    void updateOptionType() throws Exception {
        //given
        OptionRequest.UpdateOptionType request = OptionRequest.UpdateOptionType.builder()
                .name("새 이름")
                .build();
        OptionResult result = createOptionResponse().name("새 이름").build();
        OptionResponse.Detail response = OptionResponse.Detail.from(result);
        given(optionService.updateOptionTypeName(any(OptionCommand.UpdateOptionType.class)))
                .willReturn(result);
        HttpHeaders adminHeader = createAdminHeader();
        //when
        //then
        mockMvc.perform(patch("/options/{optionTypeId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(adminHeader)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(createSecuredDocument("02-option-04-update",
                        "옵션 타입 수정",
                        "옵션 타입 이름을 수정한다",
                        OptionDescriptor.getOptionUpdateRequest(),
                        OptionDescriptor.getOptionResponse(),
                        parameterWithName("optionTypeId").description("수정할 옵션 ID"))
                );
    }

    @Test
    @DisplayName("옵션을 삭제한다")
    void deleteOption() throws Exception {
        //given
        willDoNothing().given(optionService).deleteOption(anyLong());
        HttpHeaders adminHeader = createAdminHeader();
        //when
        //then
        mockMvc.perform(delete("/options/{optionTypeId}", 1L)
                        .headers(adminHeader))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(createSecuredDocument("02-option-05-delete",
                                "옵션 삭제",
                                "해당 옵션을 삭제한다",
                                parameterWithName("optionTypeId").description("삭제할 옵션 ID"))
                );
    }

    @Test
    @DisplayName("옵션 값 수정")
    void updateOptionValue() throws Exception {
        //given
        OptionRequest.UpdateOptionValue request = OptionRequest.UpdateOptionValue.builder()
                .name("새 이름")
                .build();
        OptionValueResult result = createOptionValueResponse().name("새 이름").build();
        given(optionService.updateOptionValueName(any(OptionCommand.UpdateOptionValue.class)))
                .willReturn(result);
        OptionResponse.Value response = OptionResponse.Value.from(result);
        HttpHeaders adminHeader = createAdminHeader();
        //when
        //then
        mockMvc.perform(patch("/option-values/{optionValueId}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                        .headers(adminHeader)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(createSecuredDocument("02-option-06-update-value",
                                "옵션 값 수정",
                                "옵션 값 이름을 수정한다",
                                OptionDescriptor.getOptionUpdateRequest(),
                                OptionDescriptor.getOptionValueUpdateResponse(),
                                parameterWithName("optionValueId").description("수정할 옵션 값 ID"))
                );
    }

    @Test
    @DisplayName("옵션 값 삭제")
    void deleteOptionValue() throws Exception {
        //given
        willDoNothing().given(optionService).deleteOptionValue(anyLong());
        HttpHeaders adminHeader = createAdminHeader();
        //when
        //then
        mockMvc.perform(delete("/option-values/{optionValueId}", 1L)
                .contentType(MediaType.APPLICATION_JSON).headers(adminHeader))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(createSecuredDocument("02-option-07-delete-value",
                        "옵션 값 삭제",
                        "해당 옵션 값을 삭제한다",
                        parameterWithName("optionValueId").description("삭제할 옵션 값 ID"))
                );
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
