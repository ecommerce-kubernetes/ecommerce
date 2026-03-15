package com.example.image_service.service;

import com.example.image_service.service.dto.result.PresignedUrlResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@SpringBootTest
class ImageServiceTest {

    @Autowired
    private ImageService imageService;
    @MockitoBean
    private S3Presigner s3Presigner;
    
    @Test
    @DisplayName("presigned Url과 이미지 url을 반환한다")
    void generatePresignedUrl() throws MalformedURLException {
        //given
        String presignedUrl = "http://localhost:9000/buynest-images/users/mock-uuid.jpg";
        String domain = "users";
        String originalFileName = "user_1_profiles_01.jpg";
        URL returnPresignedUrl = new URL(presignedUrl);
        PresignedPutObjectRequest mockPresignedRequest = mock(PresignedPutObjectRequest.class);
        given(mockPresignedRequest.url()).willReturn(returnPresignedUrl);
        given(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
                .willReturn(mockPresignedRequest);
        //when
        PresignedUrlResponse response = imageService.generatePresignedUrl(domain, originalFileName);
        //then
        assertThat(response.getPresignedUrl()).isEqualTo(presignedUrl);
        assertThat(response.getImageUrl()).isNotNull();
    }
}
