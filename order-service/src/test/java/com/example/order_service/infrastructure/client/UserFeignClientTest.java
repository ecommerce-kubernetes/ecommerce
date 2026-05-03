package com.example.order_service.infrastructure.client;

import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.infrastructure.dto.response.ClientErrorResponse;
import com.example.order_service.infrastructure.dto.response.UserClientResponse;
import com.example.order_service.support.TestFixtureUtil;
import com.example.order_service.support.annotation.IsolatedTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@IsolatedTest
@AutoConfigureWireMock(port = 0)
public class UserFeignClientTest {

    @Autowired
    private UserFeignClient client;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("유저 서비스에서 주문 유저 정보를 조회한다")
    void getUserInfoForOrder() throws JsonProcessingException {
        //given
        Long userId = 1L;
        UserClientResponse.OrderUserInfo mockResponse = TestFixtureUtil.giveMeOne(UserClientResponse.OrderUserInfo.class);

        //외부 서비스 호출 모킹
        stubFor(get(urlEqualTo("/internal/users/" + userId + "/order-info"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(mockResponse))));
        //when
        UserClientResponse.OrderUserInfo response = client.getUserInfoForOrder(userId);
        //then
        assertThat(response)
                .usingRecursiveComparison()
                .isEqualTo(mockResponse);
    }

    @Test
    @DisplayName("유저 서비스에서 클라이언트 오류 응답 반환시 클라이언트 예외를 던진다")
    void getUserInfoForOrder_thrown_client_error_response() throws JsonProcessingException {
        Long userId = 1L;
        //given
        ClientErrorResponse errorResponse = ClientErrorResponse.builder()
                .code("USER_CLIENT_ERROR")
                .message("잘못된 요청입니다")
                .timestamp(LocalDateTime.now())
                .path("/internal/users/" + userId + "/order-info")
                .build();

        stubFor(get(urlEqualTo("/internal/users/" + userId + "/order-info"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(errorResponse))));
        //when
        //then
        assertThatThrownBy(() -> client.getUserInfoForOrder(userId))
                .isInstanceOf(ExternalClientException.class)
                .hasMessage("잘못된 요청입니다");
    }

    @Test
    @DisplayName("유저 서비스에서 서버 오류 응답 반환시 서버 예외를 던진다")
    void getUserinfoForOrder_thrown_server_error_response() throws JsonProcessingException {
        //given
        Long userId = 1L;
        ClientErrorResponse errorResponse = ClientErrorResponse.builder()
                .code("PROD_SERVER_ERROR")
                .message("에러가 발생했습니다")
                .timestamp(LocalDateTime.now())
                .path("/internal/users/" + userId + "/order-info")
                .build();

        stubFor(get(urlEqualTo("/internal/users/" + userId + "/order-info"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(errorResponse))));
        //when
        //then
        assertThatThrownBy(() -> client.getUserInfoForOrder(userId))
                .isInstanceOf(ExternalServerException.class)
                .hasMessage("에러가 발생했습니다");
    }
}
