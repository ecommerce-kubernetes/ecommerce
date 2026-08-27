package com.example.order_service.infrastructure.client;

import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.infrastructure.dto.response.user.UserPointsResponse;
import com.example.order_service.infrastructure.dto.response.user.UserProfileResponse;
import com.example.order_service.support.annotation.IsolatedTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@IsolatedTest
@AutoConfigureWireMock(port = 0)
public class UserFeignClientTest {

    @Autowired
    private UserFeignClient client;

    private String readJson(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("사용자의 프로필 정보를 조회한다")
    void getUserProfile() throws IOException {
        //given
        String mockJsonResponse = readJson("user/profile-response.json");
        Long userId = 1L;
        stubFor(get(urlEqualTo("/internal/users/" + userId + "/profile"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(mockJsonResponse)));
        UserProfileResponse expected = createProfileResponse();
        //when
        UserProfileResponse response = client.getUserProfile(userId);
        //then
        assertThat(response)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    private UserProfileResponse createProfileResponse() {
        UserProfileResponse.ShippingAddressResponse shippingInfo = UserProfileResponse.ShippingAddressResponse.builder()
                .receiverName("수령인")
                .receiverPhone("010-1234-5678")
                .zipCode("12345")
                .address("서울시 테헤란로 123")
                .addressDetail("123동 1234호")
                .build();
        return UserProfileResponse.builder()
                .userId(1L)
                .userName("주문자")
                .phoneNumber("010-1234-5678")
                .availablePoints(1000L)
                .defaultShippingAddress(shippingInfo)
                .build();
    }

    @Test
    @DisplayName("사용자 프로필 조회시 클라이언트 에러 응답이 반환되면 예외가 발생한다")
    void getUserProfile_thrown_client_error() {
        //given
        Long userId = 1L;
        String mockJsonResponse = """
                    {
                        "code": "NOT_FOUND_USER",
                        "message": "유저를 찾을 수 없습니다",
                        "timestamp": "2026-05-03 19:00:00",
                        "path": "/internal/users/1/profile"
                    }
                    """;
        stubFor(get(urlEqualTo("/internal/users/" + userId + "/profile"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(mockJsonResponse)));
        //when
        //then
        assertThatThrownBy(() -> client.getUserProfile(userId))
                .isInstanceOf(ExternalClientException.class)
                .hasMessage("유저를 찾을 수 없습니다")
                .extracting("errorCode")
                .isEqualTo("NOT_FOUND_USER");
    }

    @Test
    @DisplayName("사용자 프로필 조회시 서버 에러 응답이 반환되면 예외가 발생한다")
    void getUserProfile_thrown_server_error() {
        //given
        Long userId = 1L;
        String mockJsonResponse = """
                    {
                        "code": "INTERNAL_SERVER_ERROR",
                        "message": "알 수 없는 오류가 발생했습니다",
                        "timestamp": "2026-05-03 19:00:00",
                        "path": "/internal/users/1/profile"
                    }
                    """;
        stubFor(get(urlEqualTo("/internal/users/" + userId + "/profile"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(mockJsonResponse)));
        //when
        //then
        assertThatThrownBy(() -> client.getUserProfile(userId))
                .isInstanceOf(ExternalServerException.class)
                .hasMessage("알 수 없는 오류가 발생했습니다")
                .extracting("errorCode")
                .isEqualTo("INTERNAL_SERVER_ERROR");
    }

    @Test
    @DisplayName("사용자의 포인트 잔액을 조회한다")
    void getUserPoints() {
        //given
        Long userId = 1L;
        String mockJsonResponse = """
                    {
                        "userId": 1,
                        "availablePoints": 10000
                    }
                    """;
        UserPointsResponse expected = UserPointsResponse.builder()
                .userId(1L)
                .availablePoints(10000L)
                .build();
        stubFor(get(urlEqualTo("/internal/users/" + userId + "/points"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(mockJsonResponse)));
        //when
        UserPointsResponse response = client.getUserPoints(userId);
        //then
        assertThat(response)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("사용자 포인트 잔액 조회시 클라이언트 에러 응답이 반환되면 예외가 발생한다")
    void getUserPoints_thrown_client_error_response() {
        //given
        Long userId = 1L;
        String mockJsonResponse = """
                    {
                        "code": "NOT_FOUND_USER",
                        "message": "유저를 찾을 수 없습니다",
                        "timestamp": "2026-05-03 19:00:00",
                        "path": "/internal/users/1/points"
                    }
                    """;
        stubFor(get(urlEqualTo("/internal/users/" + userId + "/points"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.NOT_FOUND.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(mockJsonResponse)));
        //when
        //then
        assertThatThrownBy(() -> client.getUserPoints(userId))
                .isInstanceOf(ExternalClientException.class)
                .hasMessage("유저를 찾을 수 없습니다")
                .extracting("errorCode")
                .isEqualTo("NOT_FOUND_USER");
    }

    @Test
    @DisplayName("사용자 포인트 잔액 조회시 서버 에러 응답이 반환되면 예외가 발생한다")
    void getUserPoints_thrown_server_error_response() {
        //given
        Long userId = 1L;
        String mockJsonResponse = """
                    {
                        "code": "INTERNAL_SERVER_ERROR",
                        "message": "알 수 없는 에러가 발생했습니다",
                        "timestamp": "2026-05-03 19:00:00",
                        "path": "/internal/users/1/points"
                    }
                    """;
        stubFor(get(urlEqualTo("/internal/users/" + userId + "/points"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.NOT_FOUND.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(mockJsonResponse)));
        //when
        //then
        assertThatThrownBy(() -> client.getUserPoints(userId))
                .isInstanceOf(ExternalClientException.class)
                .hasMessage("알 수 없는 에러가 발생했습니다")
                .extracting("errorCode")
                .isEqualTo("INTERNAL_SERVER_ERROR");
    }
}
