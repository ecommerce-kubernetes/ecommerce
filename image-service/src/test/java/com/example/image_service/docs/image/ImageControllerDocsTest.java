package com.example.image_service.docs.image;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.example.image_service.controller.ImageController;
import com.example.image_service.controller.dto.request.PresignedRequest;
import com.example.image_service.docs.RestDocsSupport;
import com.example.image_service.service.ImageService;
import com.example.image_service.service.dto.result.PresignedUrlResponse;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.restdocs.payload.FieldDescriptor;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ImageControllerDocsTest extends RestDocsSupport {

    private ImageService imageService = Mockito.mock(ImageService.class);

    private static final String TAG = "IMAGE";

    @Override
    protected Object initController() {
        return new ImageController(imageService);
    }

    @Test
    @DisplayName("presigned-url 발급 API")
    void getPresignedUrl() throws Exception {
        //given
        PresignedRequest request = PresignedRequest.builder()
                .domain("users")
                .originalFilename("test.jpg")
                .build();

        HttpHeaders roleAdmin = createUserHeader("ROLE_ADMIN");

        PresignedUrlResponse response = PresignedUrlResponse.builder()
                .presignedUrl("http://localhost:9000/presigned-url")
                .imageUrl("http://localhost:9000/users/test.jpg")
                .build();
        given(imageService.generatePresignedUrl(anyString(), anyString()))
                .willReturn(response);

        HeaderDescriptor[] requestHeaders = new HeaderDescriptor[] {
                headerWithName("Authorization").description("JWT Access Token")
        };

        FieldDescriptor[] requestFields = new FieldDescriptor[] {
                fieldWithPath("domain").description("이미지 도메인"),
                fieldWithPath("originalFilename").description("이미지 파일 이름")
        };

        FieldDescriptor[] responseFields = new FieldDescriptor[] {
                fieldWithPath("presignedUrl").description("cdn 이미지 추가 url"),
                fieldWithPath("imageUrl").description("이미지 url")
        };
        //when
        //then
        mockMvc.perform(post("/images/presigned-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(roleAdmin)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "01-image-01-get-presigned-url",
                                preprocessRequest(prettyPrint(),
                                        modifyHeaders()
                                                .remove("X-User-Id")
                                                .remove("X-User-Role")
                                                .add("Authorization", "Bearer {ACCESS_TOKEN}")),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag(TAG)
                                                .summary("presigned url 발급")
                                                .description("이미지 추가 presigned url을 발급한다")
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

    private HttpHeaders createUserHeader(String userRole){
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Id", "1");
        headers.add("X-User-Role", userRole);
        return headers;
    }
}
