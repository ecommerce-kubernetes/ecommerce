package com.example.product_service.docs.option;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.example.product_service.api.option.controller.OptionController;
import com.example.product_service.api.option.controller.dto.OptionCreateRequest;
import com.example.product_service.api.option.controller.dto.OptionUpdateRequest;
import com.example.product_service.api.option.service.OptionService;
import com.example.product_service.api.option.service.dto.OptionResponse;
import com.example.product_service.api.option.service.dto.OptionValueResponse;
import com.example.product_service.docs.RestDocsSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.request.ParameterDescriptor;

import java.util.List;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
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

    private static final String TAG = "Option";


    @Override
    protected Object initController() {
        return new OptionController(optionService);
    }

    @Test
    @DisplayName("옵션을 저장한다")
    void saveOption() throws Exception {
        //given
        OptionCreateRequest request = createOptionRequest();
        OptionResponse response = createOptionResponse().build();
        given(optionService.saveOption(anyString(), anyList()))
                .willReturn(response);

        HeaderDescriptor[] requestHeaders = new HeaderDescriptor[] {
                headerWithName("Authorization").description("JWT Access Token")
        };

        FieldDescriptor[] requestFields = new FieldDescriptor[] {
                fieldWithPath("name").description("옵션 이름"),
                fieldWithPath("values").description("옵션 값")
        };

        FieldDescriptor[] responseFields = new FieldDescriptor[] {
                fieldWithPath("id").description("옵션 ID"),
                fieldWithPath("name").description("옵션 이름"),
                fieldWithPath("values[].id").description("옵션 값 ID"),
                fieldWithPath("values[].name").description("옵션 값")
        };

        //when
        //then
        mockMvc.perform(post("/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(
                        document("02-option-01-create",
                                preprocessRequest(prettyPrint(),
                                        modifyHeaders()
                                                .remove("X-User-Id")
                                                .remove("X-User-Role")
                                                .add("Authorization", "Bearer {ACCESS_TOKEN}")),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag(TAG)
                                                .summary("옵션 생성")
                                                .description("새로운 옵션과 값을 생성한다")
                                                .requestHeaders(requestHeaders)
                                                .requestFields(requestFields)
                                                .responseFields(responseFields)
                                                .build()
                                ),
                                requestHeaders(requestHeaders),
                                requestFields(requestFields),
                                responseFields(responseFields)

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

        ParameterDescriptor[] pathParameters = new ParameterDescriptor[] {
                parameterWithName("optionId").description("조회할 옵션 ID")
        };

        FieldDescriptor[] responseFields = new FieldDescriptor[] {
                fieldWithPath("id").description("옵션 ID"),
                fieldWithPath("name").description("옵션 이름"),
                fieldWithPath("values[].id").description("옵션 값 ID"),
                fieldWithPath("values[].name").description("옵션 값")
        };
        //when
        //then
        mockMvc.perform(get("/options/{optionId}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("02-option-02-get",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag(TAG)
                                                .summary("옵션 조회")
                                                .description("옵션을 조회한다")
                                                .pathParameters(pathParameters)
                                                .responseFields(responseFields)
                                                .build()
                                ),
                                pathParameters(pathParameters),
                                responseFields(responseFields)
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

        FieldDescriptor[] responseFields = new FieldDescriptor[] {
                fieldWithPath("[].id").description("옵션 ID"),
                fieldWithPath("[].name").description("옵션 이름"),
                fieldWithPath("[].values[].id").description("옵션 값 ID"),
                fieldWithPath("[].values[].name").description("옵션 값")
        };
        //when
        //then
        mockMvc.perform(get("/options"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("02-option-03-get-list",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag(TAG)
                                                .summary("옵션 목록 조회")
                                                .description("옵션 목록을 조회한다")
                                                .responseFields(responseFields)
                                                .build()
                                ),
                                responseFields(responseFields)
                        )
                );
    }

    @Test
    @DisplayName("옵션을 수정한다")
    void updateOptionType() throws Exception {
        //given
        OptionUpdateRequest request = createOptionUpdateRequest("새 이름");
        OptionResponse response = createOptionResponse().name("새 이름").build();
        given(optionService.updateOptionTypeName(anyLong(), anyString()))
                .willReturn(response);

        HeaderDescriptor[] requestHeaders = new HeaderDescriptor[] {
                headerWithName("Authorization").description("JWT Access Token")
        };

        ParameterDescriptor[] pathParameters = new ParameterDescriptor[] {
            parameterWithName("optionTypeId").description("수정할 옵션 ID")
        };

        FieldDescriptor[] requestFields = new FieldDescriptor[] {
                fieldWithPath("name").description("변경할 이름")
        };

        FieldDescriptor[] responseFields = new FieldDescriptor[] {
                fieldWithPath("id").description("옵션 ID"),
                fieldWithPath("name").description("옵션 이름"),
                fieldWithPath("values[].id").description("옵션 값 ID"),
                fieldWithPath("values[].name").description("옵션 값")
        };
        //when
        //then
        mockMvc.perform(patch("/options/{optionTypeId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("02-option-04-update",
                                preprocessRequest(prettyPrint(),
                                        modifyHeaders()
                                                .remove("X-User-Id")
                                                .remove("X-User-Role")
                                                .add("Authorization", "Bearer {ACCESS_TOKEN}")),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag(TAG)
                                                .summary("옵션 타입 수정")
                                                .description("옵션 타입 이름을 수정한다")
                                                .requestHeaders(requestHeaders)
                                                .pathParameters(pathParameters)
                                                .requestFields(requestFields)
                                                .responseFields(responseFields)
                                                .build()
                                ),
                                requestHeaders(requestHeaders),
                                pathParameters(pathParameters),
                                requestFields(requestFields),
                                responseFields(responseFields)
                        )
                );
    }

    @Test
    @DisplayName("옵션을 삭제한다")
    void deleteOption() throws Exception {
        //given
        willDoNothing().given(optionService).deleteOption(anyLong());
        HeaderDescriptor[] requestHeaders = new HeaderDescriptor[] {
                headerWithName("Authorization").description("JWT Access Token")
        };

        ParameterDescriptor[] pathParameters = new ParameterDescriptor[] {
                parameterWithName("optionTypeId").description("삭제할 옵션 ID")
        };
        //when
        //then
        mockMvc.perform(delete("/options/{optionTypeId}", 1L))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(
                        document("02-option-05-delete",
                                preprocessRequest(prettyPrint(),
                                        modifyHeaders()
                                                .remove("X-User-Id")
                                                .remove("X-User-Role")
                                                .add("Authorization", "Bearer {ACCESS_TOKEN}")),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag(TAG)
                                                .summary("옵션 삭제")
                                                .description("해당 옵션을 삭제한다")
                                                .requestHeaders(requestHeaders)
                                                .pathParameters(pathParameters)
                                                .build()
                                ),
                                requestHeaders(requestHeaders),
                                pathParameters(pathParameters)
                        )
                );
    }

    @Test
    @DisplayName("옵션 값 수정")
    void updateOptionValue() throws Exception {
        //given
        OptionUpdateRequest request = createOptionUpdateRequest("새 이름");
        OptionValueResponse response = createOptionValueResponse().name("새 이름").build();
        given(optionService.updateOptionValueName(anyLong(), anyString()))
                .willReturn(response);

        HeaderDescriptor[] requestHeaders = new HeaderDescriptor[] {
                headerWithName("Authorization").description("JWT Access Token")
        };

        ParameterDescriptor[] pathParameters = new ParameterDescriptor[] {
                parameterWithName("optionValueId").description("수정할 옵션 값 ID")
        };

        FieldDescriptor[] requestFields = new FieldDescriptor[] {
                fieldWithPath("name").description("변경할 이름")
        };

        FieldDescriptor[] responseFields = new FieldDescriptor[] {
                fieldWithPath("id").description("옵션 값 ID"),
                fieldWithPath("name").description("옵션 값 이름")
        };

        //when
        //then
        mockMvc.perform(patch("/option-values/{optionValueId}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("02-option-06-update-value",
                                preprocessRequest(prettyPrint(),
                                        modifyHeaders()
                                                .remove("X-User-Id")
                                                .remove("X-User-Role")
                                                .add("Authorization", "Bearer {ACCESS_TOKEN}")),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag(TAG)
                                                .summary("옵션 값 수정")
                                                .description("옵션 값 이름을 수정한다")
                                                .requestHeaders(requestHeaders)
                                                .pathParameters(pathParameters)
                                                .requestFields(requestFields)
                                                .responseFields(responseFields)
                                                .build()
                                ),
                                requestHeaders(requestHeaders),
                                pathParameters(pathParameters),
                                requestFields(requestFields),
                                responseFields(responseFields)
                        )
                );
    }

    @Test
    @DisplayName("옵션 값 삭제")
    void deleteOptionValue() throws Exception {
        //given
        willDoNothing().given(optionService).deleteOptionValue(anyLong());

        HeaderDescriptor[] requestHeaders = new HeaderDescriptor[] {
                headerWithName("Authorization").description("JWT Access Token")
        };

        ParameterDescriptor[] pathParameters = new ParameterDescriptor[] {
                parameterWithName("optionValueId").description("삭제할 옵션 값 ID")
        };

        //when
        //then
        mockMvc.perform(delete("/option-values/{optionValueId}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(
                        document("02-option-07-delete-value",
                                preprocessRequest(prettyPrint(),
                                        modifyHeaders()
                                                .remove("X-User-Id")
                                                .remove("X-User-Role")
                                                .add("Authorization", "Bearer {ACCESS_TOKEN}")),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag(TAG)
                                                .summary("옵션 값 삭제")
                                                .requestHeaders(requestHeaders)
                                                .pathParameters(pathParameters)
                                                .build()
                                ),
                                requestHeaders(requestHeaders),
                                pathParameters(pathParameters)
                        )
                );
    }

    private OptionCreateRequest createOptionRequest() {
        return OptionCreateRequest.builder().name("사이즈").values(
                List.of("XL", "L", "M", "S")
        ).build();
    }

    private OptionUpdateRequest createOptionUpdateRequest(String name) {
        return OptionUpdateRequest.builder()
                .name(name)
                .build();
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

    private OptionValueResponse.OptionValueResponseBuilder createOptionValueResponse() {
        return OptionValueResponse.builder()
                .id(1L)
                .name("XL");
    }
}
