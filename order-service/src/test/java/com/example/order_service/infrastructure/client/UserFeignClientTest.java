package com.example.order_service.infrastructure.client;

import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.infrastructure.dto.request.UserClientRequest;
import com.example.order_service.infrastructure.dto.response.UserClientResponse;
import com.example.order_service.support.annotation.IsolatedTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.example.order_service.support.TestFixtureUtil.fixtureMonkey;
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

    @Nested
    @DisplayName("유저 주문 프로필 조회")
    class GetUserProfile {

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
            UserClientResponse.Profile expected = createExpected();
            //when
            UserClientResponse.Profile response = client.getUserProfile(userId);
            //then
            assertThat(response)
                    .usingRecursiveComparison()
                    .isEqualTo(expected);
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

        private UserClientResponse.Profile createExpected() {
            UserClientResponse.ShippingAddress shippingAddress = UserClientResponse.ShippingAddress.builder()
                    .receiverName("수령인")
                    .receiverPhone("010-1234-5678")
                    .zipCode("12345")
                    .address("서울시 테헤란로 123")
                    .addressDetail("123동 1234호")
                    .build();
            return UserClientResponse.Profile.builder()
                    .userId(1L)
                    .userName("주문자")
                    .phoneNumber("010-1234-5678")
                    .defaultShippingAddress(shippingAddress)
                    .build();
        }
    }

    @Nested
    @DisplayName("포인트 잔액 조회")
    class GetUserPoints {

        @Test
        @DisplayName("사용자의 포인트 잔액을 조회한다")
        void getUserPoints() {
            //given
            Long userId = 1L;
            Long orderAmount = 9000L;
            String mockJsonResponse = """
                    {
                        "userId": 1,
                        "ownedPoints": 10000,
                        "availablePoints": 5000
                    }
                    """;
            UserClientResponse.UserPoints expected = UserClientResponse.UserPoints.builder()
                    .userId(1L)
                    .ownedPoints(10000L)
                    .availablePoints(5000L)
                    .build();
            stubFor(get(urlEqualTo("/internal/users/" + userId + "/points?orderAmount=" + orderAmount))
                    .willReturn(aResponse()
                            .withStatus(HttpStatus.OK.value())
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .withBody(mockJsonResponse)));
            //when
            UserClientResponse.UserPoints response = client.getUserPoints(userId, orderAmount);
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
            Long orderAmount = 9000L;
            String mockJsonResponse = """
                    {
                        "code": "NOT_FOUND_USER",
                        "message": "유저를 찾을 수 없습니다",
                        "timestamp": "2026-05-03 19:00:00",
                        "path": "/internal/users/1/points"
                    }
                    """;
            stubFor(get(urlEqualTo("/internal/users/" + userId + "/points?orderAmount=" + orderAmount))
                    .willReturn(aResponse()
                            .withStatus(HttpStatus.NOT_FOUND.value())
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .withBody(mockJsonResponse)));
            //when
            //then
            assertThatThrownBy(() -> client.getUserPoints(userId, orderAmount))
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
            Long orderAmount = 9000L;
            String mockJsonResponse = """
                    {
                        "code": "INTERNAL_SERVER_ERROR",
                        "message": "알 수 없는 에러가 발생했습니다",
                        "timestamp": "2026-05-03 19:00:00",
                        "path": "/internal/users/1/points"
                    }
                    """;
            stubFor(get(urlEqualTo("/internal/users/" + userId + "/points?orderAmount=" + orderAmount))
                    .willReturn(aResponse()
                            .withStatus(HttpStatus.NOT_FOUND.value())
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .withBody(mockJsonResponse)));
            //when
            //then
            assertThatThrownBy(() -> client.getUserPoints(userId, orderAmount))
                    .isInstanceOf(ExternalClientException.class)
                    .hasMessage("알 수 없는 에러가 발생했습니다")
                    .extracting("errorCode")
                    .isEqualTo("INTERNAL_SERVER_ERROR");
        }
    }

    @Test
    @DisplayName("유저 서비스에서 주문 유저 정보를 조회한다")
    void getUserInfoForOrder() {
        //given
        Long userId = 1L;
        String mockJsonResponse = """
                {
                    "userId": 1,
                    "pointBalance": 10000,
                    "userName": "유저",
                    "phoneNumber" : "010-1234-5678"
                }
                """;

        //외부 서비스 호출 모킹
        stubFor(get(urlEqualTo("/internal/users/" + userId + "/order-info"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(mockJsonResponse)));
        //when
        UserClientResponse.UserInfo response = client.getUserInfoForOrder(userId);
        //then
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.pointBalance()).isEqualTo(10000L);
        assertThat(response.userName()).isEqualTo("유저");
        assertThat(response.phoneNumber()).isEqualTo("010-1234-5678");
    }

    @Test
    @DisplayName("유저 서비스에서 클라이언트 오류 응답 반환시 클라이언트 예외를 던진다")
    void getUserInfoForOrder_thrown_client_error_response() {
        Long userId = 1L;
        //given
        String mockJsonResponse = """
                {
                    "code": "NOT_FOUND_USER",
                    "message": "유저를 찾을 수 없습니다",
                    "timestamp": "2026-05-03 19:00:00",
                    "path": "/internal/users/1/order-info"
                }
                """;
        stubFor(get(urlEqualTo("/internal/users/" + userId + "/order-info"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.NOT_FOUND.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(mockJsonResponse)));
        //when
        //then
        assertThatThrownBy(() -> client.getUserInfoForOrder(userId))
                .isInstanceOf(ExternalClientException.class)
                .hasMessage("유저를 찾을 수 없습니다")
                .extracting("errorCode")
                .isEqualTo("NOT_FOUND_USER");
    }

    @Test
    @DisplayName("유저 서비스에서 서버 오류 응답 반환시 서버 예외를 던진다")
    void getUserinfoForOrder_thrown_server_error_response() {
        //given
        Long userId = 1L;
        String mockJsonResponse = """
                {
                    "code": "INTERNAL_SERVER_ERROR",
                    "message": "처리중 오류가 발생했습니다",
                    "timestamp": "2026-05-03 19:00:00",
                    "path": "/internal/users/1/order-info"
                }
                """;

        stubFor(get(urlEqualTo("/internal/users/" + userId + "/order-info"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(mockJsonResponse)));
        //when
        //then
        assertThatThrownBy(() -> client.getUserInfoForOrder(userId))
                .isInstanceOf(ExternalServerException.class)
                .hasMessage("처리중 오류가 발생했습니다")
                .extracting("errorCode")
                .isEqualTo("INTERNAL_SERVER_ERROR");
    }

    @Nested
    @DisplayName("포인트 사용 검증")
    class GetUserPointsForOrder {

        @Test
        @DisplayName("포인트 정보를 조회한다")
        void getUserPointsForOrder() {
            //given
            Long userId = 1L;
            UserClientRequest.ValidatePoints request = UserClientRequest.ValidatePoints.builder()
                    .orderAmount(10000L)
                    .usedPoints(3000L)
                    .build();
            String mockJsonResponse = """
                    {
                        "userId": 1,
                        "ownedPoints": 10000,
                        "availablePoints": 5000
                    }
                    """;
            UserClientResponse.UserPoints expected = UserClientResponse.UserPoints.builder()
                    .userId(1L)
                    .ownedPoints(10000L)
                    .availablePoints(5000L)
                    .build();
            stubFor(post(urlEqualTo("/internal/users/" + userId + "/points/validate-for-order"))
                    .willReturn(aResponse()
                            .withStatus(HttpStatus.OK.value())
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .withBody(mockJsonResponse)));
            //when
            UserClientResponse.UserPoints response = client.getUserPointsForOrder(userId, request);
            //then
            assertThat(response).usingRecursiveComparison().isEqualTo(expected);
        }

        @Test
        @DisplayName("포인트 정보 조회중 클라이언트 에러 응답이 반환되면 예외가 발생한다")
        void getUserPointsForOrder_thrown_client_error() {
            //given
            Long userId = 1L;
            UserClientRequest.ValidatePoints request = UserClientRequest.ValidatePoints.builder()
                    .orderAmount(10000L)
                    .usedPoints(3000L)
                    .build();
            String mockJsonResponse = """
                    {
                    "code": "NOT_FOUND_USER",
                    "message": "유저를 찾을 수 없습니다",
                    "timestamp": "2026-05-03 19:00:00",
                    "path": "/internal/users/1/points/validate-for-order"
                    }
                    """;
            stubFor(post(urlEqualTo("/internal/users/" + userId + "/points/validate-for-order"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .withStatus(HttpStatus.BAD_REQUEST.value())
                            .withBody(mockJsonResponse)));
            //when
            //then
            assertThatThrownBy(() -> client.getUserPointsForOrder(userId, request))
                    .isInstanceOf(ExternalClientException.class)
                    .hasMessage("유저를 찾을 수 없습니다")
                    .extracting("errorCode")
                    .isEqualTo("NOT_FOUND_USER");
        }

        @Test
        @DisplayName("포인트 정보 조회중 서버 오류 응답이 반환되면 예외가 발생한다")
        void getUserPointsForOrder_thrown_server_error() {
            //given
            Long userId = 1L;
            UserClientRequest.ValidatePoints request = UserClientRequest.ValidatePoints.builder()
                    .orderAmount(10000L)
                    .usedPoints(3000L)
                    .build();
            String mockJsonResponse = """
                    {
                    "code": "INTERNAL_SERVER_ERROR",
                    "message": "알 수 없는 에러가 발생했습니다",
                    "timestamp": "2026-05-03 19:00:00",
                    "path": "/internal/users/1/points/validate-for-order"
                    }
                    """;

            stubFor(post(urlEqualTo("/internal/users/" + userId + "/points/validate-for-order"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .withBody(mockJsonResponse)));
            //when
            //then
            assertThatThrownBy(() -> client.getUserPointsForOrder(userId, request))
                    .isInstanceOf(ExternalServerException.class)
                    .hasMessage("알 수 없는 에러가 발생했습니다")
                    .extracting("errorCode")
                    .isEqualTo("INTERNAL_SERVER_ERROR");
        }
    }
}
