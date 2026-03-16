package com.example.image_service.controller;

import com.example.image_service.common.security.model.UserRole;
import com.example.image_service.controller.dto.request.PresignedRequest;
import com.example.image_service.service.dto.result.PresignedUrlResponse;
import com.example.image_service.support.ControllerTestSupport;
import com.example.image_service.support.security.annotation.WithCustomMockUser;
import com.example.image_service.support.security.config.TestSecurityConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestSecurityConfig.class)
public class ImageControllerTest extends ControllerTestSupport {

    @Test
    @DisplayName("presignedUrl 과 imageUrl을 발급 받는다")
    @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
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

    @Test
    @DisplayName("이미지 url을 발급 받으려면 관리자 권한이여야 한다")
    @WithCustomMockUser(userRole = UserRole.ROLE_USER)
    void getPresignedUrl_userRole() throws Exception {
        //given
        PresignedRequest request = createPresignedRequest().build();
        //when
        //then
        mockMvc.perform(post("/images/presigned-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("FORBIDDEN"))
                .andExpect(jsonPath("message").value("요청 권한이 부족합니다"))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/images/presigned-url"));
    }

    @Test
    @DisplayName("로그인 하지 않은 유저는 이미지 url을 발급 받을 수 없다")
    void getPresignedUrl_unAuthentication() throws Exception {
        //given
        PresignedRequest request = createPresignedRequest().build();
        //when
        //then
        mockMvc.perform(post("/images/presigned-url")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("message").value("인증이 필요한 접근입니다"))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/images/presigned-url"));
    }

    @ParameterizedTest(name = "{0}")
    @WithCustomMockUser(userRole = UserRole.ROLE_ADMIN)
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
