package com.example.order_service.api.common.client.product;

import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.ExternalServiceErrorCode;
import feign.Request;
import feign.Response;
import feign.Util;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ProductErrorDecoderTest {

    private final ProductErrorDecoder decoder = new ProductErrorDecoder();

    @Test
    @DisplayName("404 응답이 오면 not Found 예외를 던진다")
    void decodeWhen404Code(){
        //given
        Response response = createResponse(404, "Not Found");
        //when
        Exception exception = decoder.decode("key", response);
        //then
        assertThat(exception).isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ExternalServiceErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("500 응답이 오면 서버 장애 예외를 던진다")
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
