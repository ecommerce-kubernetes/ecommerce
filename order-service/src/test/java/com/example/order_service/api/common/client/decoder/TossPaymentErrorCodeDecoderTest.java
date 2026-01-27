package com.example.order_service.api.common.client.decoder;

import com.example.order_service.api.common.client.payment.TossErrorResponse;
import com.example.order_service.api.common.client.payment.TossPaymentErrorDecoder;
import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.ErrorCode;
import com.example.order_service.api.common.exception.PaymentErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
import feign.Util;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class TossPaymentErrorCodeDecoderTest {
    private TossPaymentErrorDecoder decoder = new TossPaymentErrorDecoder();
    private static ObjectMapper objectMapper = new ObjectMapper();


    @ParameterizedTest(name = "{0}")
    @DisplayName("토스 에러가 발생한 경우 그에 맞는 예외로 변환해 던진다")
    @MethodSource("provideTossErrorCase")
    void decoder(String description, Response response, ErrorCode errorCode) {
        //given
        //when
        Exception exception = decoder.decode("key", response);
        //then
        assertThat(exception).isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }

    private static Response createResponse(int status, TossErrorResponse response) throws JsonProcessingException {
        String jsonResponse = objectMapper.writeValueAsString(response);
        return Response.builder()
                .status(status)
                .request(Request.create(
                        Request.HttpMethod.GET,
                        "/api/test",
                        Collections.emptyMap(),
                        null,
                        Util.UTF_8,
                        null
                ))
                .headers(Map.of())
                .body(jsonResponse, Util.UTF_8)
                .build();
    }

    private static TossErrorResponse createErrorResponse(String code, String message) {
        return TossErrorResponse.builder()
                .code(code)
                .message(message)
                .build();
    }

    private static Stream<Arguments> provideTossErrorCase() throws JsonProcessingException {
        return Stream.of(
                Arguments.of(
                        "이미 처리된 결제",
                        createResponse(400, createErrorResponse("ALREADY_PROCESSED_PAYMENT", "이미 처리된 결제 입니다.")),
                        PaymentErrorCode.PAYMENT_ALREADY_PROCEED_PAYMENT),
                Arguments.of(
                        "잘못된 요청",
                        createResponse(400, createErrorResponse("INVALID_REQUEST", "잘못된 요청입니다.")),
                        PaymentErrorCode.PAYMENT_BAD_REQUEST
                ),
                Arguments.of(
                        "잘못된 시크릿 키 연동",
                        createResponse(400, createErrorResponse("INVALID_API_KEY", "잘못된 시크릿키 연동 정보 입니다.")),
                        PaymentErrorCode.PAYMENT_SYSTEM_ERROR
                ),
                Arguments.of("401 인증 에러",
                        createResponse(401, createErrorResponse("UNAUTHORIZED_KEY", "인증되지 않은 시크릿 키 혹은 클라이언트 키 입니다.")),
                        PaymentErrorCode.PAYMENT_SYSTEM_ERROR),
                Arguments.of("잔액 부족",
                        createResponse(403, createErrorResponse("REJECT_ACCOUNT_PAYMENT", "잔액부족으로 결제에 실패했습니다.")),
                        PaymentErrorCode.PAYMENT_INSUFFICIENT_BALANCE),
                Arguments.of("허용되지 않은 요청",
                        createResponse(403, createErrorResponse("FORBIDDEN_REQUEST", "허용되지 않은 요청입니다")),
                        PaymentErrorCode.PAYMENT_SYSTEM_ERROR),

                Arguments.of("결제 정보 찾을 수 없음",
                        createResponse(404, createErrorResponse("NOT_FOUND_PAYMENT", "존재하지 않는 결제 정보 입니다.")),
                        PaymentErrorCode.PAYMENT_NOT_FOUND),
                Arguments.of("결제 시간이 만료",
                        createResponse(404, createErrorResponse("NOT_FOUND_PAYMENT_SESSION", "결제 시간이 만료되어 결제 진행 데이터가 존재하지 않습니다.")),
                        PaymentErrorCode.PAYMENT_TIMEOUT)
        );
    }
}
