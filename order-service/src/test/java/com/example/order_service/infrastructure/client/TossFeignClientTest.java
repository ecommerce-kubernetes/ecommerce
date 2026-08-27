package com.example.order_service.infrastructure.client;

import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.infrastructure.dto.request.TossCancelRequest;
import com.example.order_service.infrastructure.dto.request.TossConfirmRequest;
import com.example.order_service.infrastructure.dto.response.pg.TossCancelResponse;
import com.example.order_service.infrastructure.dto.response.pg.TossConfirmResponse;
import com.example.order_service.infrastructure.dto.response.pg.TossInquiryResponse;
import com.example.order_service.support.annotation.IsolatedTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.OffsetDateTime;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@IsolatedTest
@AutoConfigureWireMock(port = 0)
public class TossFeignClientTest {

    @Autowired
    private TossFeignClient client;
    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("payment.toss.url", () -> "http://localhost:${wiremock.server.port}");
    }

    @Test
    @DisplayName("토스 결제 승인을 요청한다")
    void confirmPayment() throws JsonProcessingException {
        //given
        TossConfirmRequest request = Instancio.create(TossConfirmRequest.class);
        String expectedRequestBody = objectMapper.writeValueAsString(request);
        String mockJsonResponse = """
                    {
                        "status": "DONE",
                        "method": "카드",
                        "totalAmount": 1000,
                        "approvedAt": "2024-02-13T12:18:14+09:00",
                        "lastTransactionKey": "9C62B18EEF0DE3EB7F4422EB6D14BC6E"
                    }
                    """;
        stubFor(post(urlEqualTo("/v1/payments/confirm"))
                .withRequestBody(equalToJson(expectedRequestBody))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockJsonResponse)));
        //when
        TossConfirmResponse response = client.confirmPayment(request);
        //then
        assertThat(response.status()).isEqualTo("DONE");
        assertThat(response.method()).isEqualTo("카드");
        assertThat(response.totalAmount()).isEqualTo(1000);
        assertThat(response.lastTransactionKey()).isEqualTo("9C62B18EEF0DE3EB7F4422EB6D14BC6E");
        assertThat(response.approvedAt())
                .isEqualTo(OffsetDateTime.parse("2024-02-13T12:18:14+09:00"));
    }

    @Test
    @DisplayName("토스 결제 승인을 요청할때 헤더에 시크릿 키를 포함하여 요청한다")
    void confirmPayment_header_contain_auth_key() throws JsonProcessingException {
        //given
        TossConfirmRequest request = Instancio.create(TossConfirmRequest.class);
        String expectedRequestBody = objectMapper.writeValueAsString(request);
        String mockJsonResponse = """
                    {
                        "status": "DONE",
                        "method": "카드",
                        "totalAmount": 1000,
                        "approvedAt": "2024-02-13T12:18:14+09:00",
                        "lastTransactionKey": "9C62B18EEF0DE3EB7F4422EB6D14BC6E"
                    }
                    """;
        stubFor(post(urlEqualTo("/v1/payments/confirm"))
                .withRequestBody(equalToJson(expectedRequestBody))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockJsonResponse)));
        //when
        client.confirmPayment(request);
        //then
        verify(postRequestedFor(urlMatching("/v1/payments/confirm"))
                .withHeader("Authorization", matching("Basic .*")));
    }

    @Test
    @DisplayName("토스 페이먼츠에서 클라이언트 오류 응답 반환시 클라이언트 예외를 던진다")
    void confirmPayment_thrown_client_error_response() throws JsonProcessingException {
        //given
        TossConfirmRequest request = Instancio.create(TossConfirmRequest.class);
        String expectedRequestBody = objectMapper.writeValueAsString(request);
        String mockJsonResponse = """
                    {
                        "code": "ALREADY_PROCESSED_PAYMENT",
                        "message": "이미 처리된 결제 입니다."
                    }
                    """;
        stubFor(post(urlEqualTo("/v1/payments/confirm"))
                .withRequestBody(equalToJson(expectedRequestBody))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockJsonResponse)));
        //when
        //then
        assertThatThrownBy(() -> client.confirmPayment(request))
                .isInstanceOf(ExternalClientException.class)
                .hasMessage("이미 처리된 결제 입니다.")
                .extracting("errorCode")
                .isEqualTo("ALREADY_PROCESSED_PAYMENT");
    }

    @Test
    @DisplayName("토스 페이먼츠에서 서버 오류 응답 반환시 서버 예외를 던진다")
    void confirmPayment_thrown_server_error_response() throws JsonProcessingException {
        //given
        TossConfirmRequest request = Instancio.create(TossConfirmRequest.class);
        String expectedRequestBody = objectMapper.writeValueAsString(request);
        String mockJsonResponse = """
                    {
                        "code": "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING",
                        "message": "결제가 완료되지 않았어요. 다시 시도해주세요."
                    }
                    """;

        stubFor(post(urlEqualTo("/v1/payments/confirm"))
                .withRequestBody(equalToJson(expectedRequestBody))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockJsonResponse)));
        //when
        //then
        assertThatThrownBy(() -> client.confirmPayment(request))
                .isInstanceOf(ExternalClientException.class)
                .hasMessage("결제가 완료되지 않았어요. 다시 시도해주세요.")
                .extracting("errorCode")
                .isEqualTo("FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING");
    }

    @Test
    @DisplayName("토스 결제 취소를 요청한다")
    void cancelPayment() throws JsonProcessingException {
        //given
        String paymentKey = "5EnNZRJGvaBX7zk2yd8ydw26XvwXkLrx9POLqKQjmAw4b0e1";
        TossCancelRequest request = Instancio.create(TossCancelRequest.class);
        String expectedRequestBody = objectMapper.writeValueAsString(request);
        String mockJsonResponse = """
                    {
                        "status": "CANCELED",
                        "cancels": [
                          {
                            "transactionKey": "090A796806E726BBB929F4A2CA7DB9A7",
                            "cancelReason": "테스트 결제 취소",
                            "canceledAt": "2024-02-13T12:20:23+09:00",
                            "cancelAmount": 1000
                          }
                        ]
                    }
                    """;
        stubFor(post(urlEqualTo("/v1/payments/" + paymentKey + "/cancel"))
                .withRequestBody(equalToJson(expectedRequestBody))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockJsonResponse)));
        //when
        TossCancelResponse response = client.cancelPayment(paymentKey, request);
        //then
        assertThat(response.status()).isEqualTo("CANCELED");
        TossCancelResponse.CancelReceipt cancel = response.cancels().getFirst();

        assertThat(cancel.transactionKey()).isEqualTo("090A796806E726BBB929F4A2CA7DB9A7");
        assertThat(cancel.cancelReason()).isEqualTo("테스트 결제 취소");
        assertThat(cancel.cancelAmount()).isEqualTo(1000L);
        assertThat(cancel.canceledAt())
                .isEqualTo(OffsetDateTime.parse("2024-02-13T12:20:23+09:00"));
    }

    @Test
    @DisplayName("토스 결제 취소를 요청할때 헤더에 시크릿 키를 포함하여 요청한다")
    void cancelPayment_header_contain_auth_key() throws JsonProcessingException {
        //given
        String paymentKey = "5EnNZRJGvaBX7zk2yd8ydw26XvwXkLrx9POLqKQjmAw4b0e1";
        TossCancelRequest request = Instancio.create(TossCancelRequest.class);
        String expectedRequestBody = objectMapper.writeValueAsString(request);
        String mockJsonResponse = """
                    {
                        "status": "CANCELED",
                        "cancels": [
                          {
                            "transactionKey": "090A796806E726BBB929F4A2CA7DB9A7",
                            "cancelReason": "테스트 결제 취소",
                            "canceledAt": "2024-02-13T12:20:23+09:00",
                            "cancelAmount": 1000
                          }
                        ]
                    }
                    """;
        stubFor(post(urlEqualTo("/v1/payments/" + paymentKey + "/cancel"))
                .withRequestBody(equalToJson(expectedRequestBody))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockJsonResponse)));
        //when
        client.cancelPayment(paymentKey, request);
        //then
        verify(postRequestedFor(urlMatching("/v1/payments/" + paymentKey + "/cancel"))
                .withHeader("Authorization", matching("Basic .*")));
    }

    @Test
    @DisplayName("토스 페이먼츠에서 클라이언트 오류 응답 반환시 클라이언트 예외를 던진다")
    void cancelPayment_thrown_client_error_response() throws JsonProcessingException {
        //given
        String paymentKey = "5EnNZRJGvaBX7zk2yd8ydw26XvwXkLrx9POLqKQjmAw4b0e1";
        TossCancelRequest request = Instancio.create(TossCancelRequest.class);
        String expectedRequestBody = objectMapper.writeValueAsString(request);
        String mockJsonResponse = """
                    {
                        "code": "ALREADY_CANCELED_PAYMENT",
                        "message": "이미 취소된 결제 입니다."
                    }
                    """;
        stubFor(post(urlEqualTo("/v1/payments/" + paymentKey + "/cancel"))
                .withRequestBody(equalToJson(expectedRequestBody))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockJsonResponse)));
        //when
        //then
        assertThatThrownBy(() -> client.cancelPayment(paymentKey, request))
                .isInstanceOf(ExternalClientException.class)
                .hasMessage("이미 취소된 결제 입니다.")
                .extracting("errorCode")
                .isEqualTo("ALREADY_CANCELED_PAYMENT");
    }

    @Test
    @DisplayName("토스 페이먼츠에서 서버 오류 응답 반환시 서버 예외를 던진다")
    void cancelPayment_thrown_server_error_response() throws JsonProcessingException {
        //given
        String paymentKey = "5EnNZRJGvaBX7zk2yd8ydw26XvwXkLrx9POLqKQjmAw4b0e1";
        TossCancelRequest request = Instancio.create(TossCancelRequest.class);
        String expectedRequestBody = objectMapper.writeValueAsString(request);
        String mockJsonResponse = """
                    {
                        "code": "FAILED_REFUND_PROCESS",
                        "message": "은행 응답시간 지연이나 일시적인 오류로 환불요청에 실패했습니다."
                    }
                    """;

        stubFor(post(urlEqualTo("/v1/payments/" + paymentKey + "/cancel"))
                .withRequestBody(equalToJson(expectedRequestBody))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockJsonResponse)));
        //when
        //then
        assertThatThrownBy(() -> client.cancelPayment(paymentKey, request))
                .isInstanceOf(ExternalServerException.class)
                .hasMessage("은행 응답시간 지연이나 일시적인 오류로 환불요청에 실패했습니다.")
                .extracting("errorCode")
                .isEqualTo("FAILED_REFUND_PROCESS");
    }

    @Test
    @DisplayName("토스 결제 조회를 요청한다")
    void inquiryPayment() {
        //given
        String paymentKey = "5EnNZRJGvaBX7zk2yd8ydw26XvwXkLrx9POLqKQjmAw4b0e1";
        String mockJsonResponse = """
                    {
                        "lastTransactionKey": "9C62B18EEF0DE3EB7F4422EB6D14BC6E",
                        "status": "CANCELED",
                        "failure": null,
                        "cancels": [
                          {
                            "transactionKey": "090A796806E726BBB929F4A2CA7DB9A7",
                            "cancelReason": "테스트 결제 취소",
                            "canceledAt": "2024-02-13T12:20:23+09:00",
                            "cancelAmount": 1000
                          }
                        ]
                    }
                    """;
        stubFor(get(urlEqualTo("/v1/payments/" + paymentKey))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockJsonResponse)));
        //when
        TossInquiryResponse response = client.inquiryPayment(paymentKey);
        //then
        assertThat(response.status()).isEqualTo("CANCELED");
        assertThat(response.lastTransactionKey()).isEqualTo("9C62B18EEF0DE3EB7F4422EB6D14BC6E");
        assertThat(response.failure()).isNull();

        TossInquiryResponse.CancelReceipt cancel = response.cancels().getFirst();
        assertThat(cancel.transactionKey()).isEqualTo("090A796806E726BBB929F4A2CA7DB9A7");
        assertThat(cancel.cancelReason()).isEqualTo("테스트 결제 취소");
        assertThat(cancel.cancelAmount()).isEqualTo(1000L);
        assertThat(cancel.canceledAt())
                .isEqualTo(OffsetDateTime.parse("2024-02-13T12:20:23+09:00"));
    }

    @Test
    @DisplayName("토스 결제 조회를 요청할때 헤더에 시크릿 키를 포함하여 요청한다")
    void inquiryPayment_header_contain_auth_key() {
        //given
        String paymentKey = "5EnNZRJGvaBX7zk2yd8ydw26XvwXkLrx9POLqKQjmAw4b0e1";
        String mockJsonResponse = """
                    {
                        "lastTransactionKey": "9C62B18EEF0DE3EB7F4422EB6D14BC6E",
                        "status": "DONE",
                        "failure": null,
                        "cancels": null
                    }
                    """;
        stubFor(get(urlEqualTo("/v1/payments/" + paymentKey))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockJsonResponse)));
        //when
        client.inquiryPayment(paymentKey);
        //then
        verify(getRequestedFor(urlMatching("/v1/payments/" + paymentKey))
                .withHeader("Authorization", matching("Basic .*")));
    }

    @Test
    @DisplayName("토스 페이먼츠에서 클라이언트 오류 응답 반환시 클라이언트 예외를 던진다")
    void inquiryPayment_thrown_client_error_response() {
        //given
        String paymentKey = "5EnNZRJGvaBX7zk2yd8ydw26XvwXkLrx9POLqKQjmAw4b0e1";
        String mockJsonResponse = """
                    {
                        "code": "NOT_FOUND_PAYMENT",
                        "message": "존재하지 않는 결제 입니다."
                    }
                    """;
        stubFor(get(urlEqualTo("/v1/payments/" + paymentKey))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockJsonResponse)));
        //when
        //then
        assertThatThrownBy(() -> client.inquiryPayment(paymentKey))
                .isInstanceOf(ExternalClientException.class)
                .hasMessage("존재하지 않는 결제 입니다.")
                .extracting("errorCode")
                .isEqualTo("NOT_FOUND_PAYMENT");
    }

    @Test
    @DisplayName("토스 페이먼츠에서 서버 오류 응답 반환시 서버 예외를 던진다")
    void inquiryPayment_thrown_server_error_response() {
        //given
        String paymentKey = "5EnNZRJGvaBX7zk2yd8ydw26XvwXkLrx9POLqKQjmAw4b0e1";
        String mockJsonResponse = """
                    {
                        "code": "FAILED_INTERNAL_SYSTEM_PROCESSING",
                        "message": "일시적인 시스템 오류로 결제 조회에 실패했습니다."
                    }
                    """;
        stubFor(get(urlEqualTo("/v1/payments/" + paymentKey))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockJsonResponse)));
        //when
        //then
        assertThatThrownBy(() -> client.inquiryPayment(paymentKey))
                .isInstanceOf(ExternalServerException.class)
                .hasMessage("일시적인 시스템 오류로 결제 조회에 실패했습니다.")
                .extracting("errorCode")
                .isEqualTo("FAILED_INTERNAL_SYSTEM_PROCESSING");
    }
}
