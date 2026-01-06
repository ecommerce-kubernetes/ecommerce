package com.example.order_service.api.common.client.decoder;

import com.example.order_service.api.common.client.user.UserErrorDecoder;
import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.CommonErrorCode;
import com.example.order_service.api.common.exception.ExternalServiceErrorCode;
import feign.Request;
import feign.Response;
import feign.Util;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class UserErrorDecoderTest {

    private final UserErrorDecoder decoder = new UserErrorDecoder();

    @Test
    @DisplayName("404 응답이 오면 NotFoundException을 반환한다")
    void decodeWhen404Code(){
        //given
        Response response = createResponse(404, "Not Found");
        //when
        Exception exception = decoder.decode("key", response);
        //then
        assertThat(exception).isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ExternalServiceErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("500 응답이 오면 InternalServerException을 반환한다")
    void decodeWhen500Code(){
        //given
        Response response = createResponse(500, "Server Error");
        //when
        Exception exception = decoder.decode("key", response);
        //then
        assertThat(exception).isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ExternalServiceErrorCode.SYSTEM_ERROR);
    }

    @Test
    @DisplayName("알 수 없는 에러 발생시 UNKNOWN_ERROR 예외를 던진다")
    void decodeWhenUnKnownError(){
        //given
        Response response = createResponse(401, "Server Error");
        //when
        Exception exception = decoder.decode("key", response);
        //then
        assertThat(exception).isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CommonErrorCode.UNKNOWN_ERROR);
    }

    private Response createResponse(int status, String message){
        return Response.builder()
                .status(status)
                .reason(message)
                .request(Request.create(
                        Request.HttpMethod.GET,
                        "/api/test",
                        Collections.emptyMap(),
                        null,
                        Util.UTF_8,
                        null
                ))
                .headers(Map.of())
                .build();
    }
}
