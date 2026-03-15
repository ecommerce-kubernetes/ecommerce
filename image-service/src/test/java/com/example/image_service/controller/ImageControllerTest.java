package com.example.image_service.controller;


import com.example.image_service.controller.dto.request.PresignedRequest;
import com.example.image_service.service.dto.result.PresignedUrlResponse;
import com.example.image_service.support.ControllerTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ImageControllerTest extends ControllerTestSupport {

    @Test
    @DisplayName("presignedUrl 과 imageUrl을 발급 받는다")
    void getPresignedUrl() throws Exception {
        //given
        String presignedUrl = "http://localhost:9000/buynest-images/users/mock-uuid.jpg";
        String imageUrl = "http://localhost:9000/buynest-image/users/uuid.jpg";
        PresignedUrlResponse response = PresignedUrlResponse.builder()
                .presignedUrl(presignedUrl)
                .imageUrl(imageUrl)
                .build();
        given(imageService.generatePresignedUrl(anyString(), anyString()))
                .willReturn(response);
        PresignedRequest request = createPresignedRequest().build();
        //when
        //then
        mockMvc.perform(post("/images/presigned-url")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInvalidPresignedRequest")
    @DisplayName("presigned 발급 요청 검증")
    void getPresignedUrl_Validation(String description, PresignedRequest request, String message) throws Exception {
        //given
        //when
        //then
        mockMvc.perform(post("/images/presigned-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value("VALIDATION"))
                .andExpect(jsonPath("message").value(message));
    }

    private static Stream<Arguments> provideInvalidPresignedRequest() {
        return Stream.of(
                Arguments.of("domain 은 필수값이여야 한다",
                        createPresignedRequest().domain("").build(),
                        "domain은 필수값 입니다"
                ),
                Arguments.of("originalFilename 은 필수값이여야 한다",
                        createPresignedRequest().originalFilename("").build(),
                        "originalFilename 은 필수값 입니다")
        );
    }

    private static PresignedRequest.PresignedRequestBuilder createPresignedRequest() {
        return PresignedRequest.builder()
                .domain("users")
                .originalFilename("image.jpg");
    }
}
