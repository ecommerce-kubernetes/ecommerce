package com.example.product_service.docs.option;

import com.example.product_service.api.option.controller.OptionController;
import com.example.product_service.api.option.controller.dto.OptionRequest;
import com.example.product_service.api.option.service.OptionService;
import com.example.product_service.api.option.service.dto.OptionResponse;
import com.example.product_service.api.option.service.dto.OptionValueResponse;
import com.example.product_service.docs.RestDocsSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OptionControllerDocsTest extends RestDocsSupport {
    OptionService optionService = Mockito.mock(OptionService.class);

    @Override
    protected Object initController() {
        return new OptionController(optionService);
    }

    @Test
    @DisplayName("옵션을 저장한다")
    void saveOption() throws Exception {
        //given
        OptionRequest request = createOptionRequest();
        OptionResponse response = createOptionResponse().build();
        given(optionService.saveOption(anyString(), anyList()))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(post("/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(
                        document("create-option",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("X-User-Id").description(USER_ID_HEADER_DESCRIPTION).optional(),
                                        headerWithName("X-User-Role").description(USER_ROLE_HEADER_DESCRIPTION).optional()
                                ),
                                requestFields(
                                        fieldWithPath("name").description("옵션 이름").optional(),
                                        fieldWithPath("values").description("옵션 값")
                                ),
                                responseFields(
                                        fieldWithPath("id").description("옵션 ID"),
                                        fieldWithPath("name").description("옵션 이름"),
                                        fieldWithPath("values[].id").description("옵션 값 ID"),
                                        fieldWithPath("values[].name").description("옵션 값")
                                )

                        )
                );
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
        mockMvc.perform(get("/options/{optionId}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("get-option",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("optionId").description("조회할 옵션 ID")
                                ),
                                responseFields(
                                        fieldWithPath("id").description("옵션 ID"),
                                        fieldWithPath("name").description("옵션 이름"),
                                        fieldWithPath("values[].id").description("옵션 값 ID"),
                                        fieldWithPath("values[].name").description("옵션 값")
                                )
                        )
                );
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
        mockMvc.perform(get("/options"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("get-options",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                responseFields(
                                        fieldWithPath("[].id").description("옵션 ID"),
                                        fieldWithPath("[].name").description("옵션 이름"),
                                        fieldWithPath("[].values[].id").description("옵션 값 ID"),
                                        fieldWithPath("[].values[].name").description("옵션 값")
                                )
                        )
                );
    }

    @Test
    @DisplayName("옵션을 수정한다")
    void updateOption() throws Exception {
        //given
        OptionRequest request = createOptionRequest();
        OptionResponse response = createOptionResponse().build();
        given(optionService.updateOption(anyLong(), anyString(), anyList()))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(put("/options/{optionId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("update-option",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("X-User-Id").description(USER_ID_HEADER_DESCRIPTION).optional(),
                                        headerWithName("X-User-Role").description(USER_ROLE_HEADER_DESCRIPTION).optional()
                                ),
                                pathParameters(
                                        parameterWithName("optionId").description("수정할 옵션 ID")
                                ),
                                requestFields(
                                        fieldWithPath("name").description("옵션 이름").optional(),
                                        fieldWithPath("values").description("옵션 값")
                                ),
                                responseFields(
                                        fieldWithPath("id").description("옵션 ID"),
                                        fieldWithPath("name").description("옵션 이름"),
                                        fieldWithPath("values[].id").description("옵션 값 ID"),
                                        fieldWithPath("values[].name").description("옵션 값")
                                )

                        )
                );
    }

    @Test
    @DisplayName("옵션을 삭제한다")
    void deleteOption() throws Exception {
        //given
        willDoNothing().given(optionService).deleteOption(anyLong());
        //when
        //then
        mockMvc.perform(delete("/options/{optionId}", 1L))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(
                        document("delete-option",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("X-User-Id").description(USER_ID_HEADER_DESCRIPTION).optional(),
                                        headerWithName("X-User-Role").description(USER_ROLE_HEADER_DESCRIPTION).optional()
                                ),
                                pathParameters(
                                        parameterWithName("optionId").description("삭제할 옵션 ID")
                                )
                        )
                );
    }

    private OptionRequest createOptionRequest() {
        return OptionRequest.builder().name("사이즈").values(
                List.of("XL", "L", "M", "S")
        ).build();
    }

    private OptionResponse.OptionResponseBuilder createOptionResponse() {
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
}
