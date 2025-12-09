package com.example.order_service.api.common.client.product;

import com.example.order_service.api.common.client.coupon.CouponErrorDecoder;
import com.example.order_service.api.common.exception.NotFoundException;
import com.example.order_service.api.common.exception.server.InternalServerException;
import feign.Request;
import feign.Response;
import feign.Util;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CouponErrorDecoderTest {
    private final CouponErrorDecoder decoder = new CouponErrorDecoder();

    @Test
    @DisplayName("404 응답이 오면 NotFoundException을 반환한다")
    void decodeWhen404Code(){
        //given
        Response response = createResponse(404, "Not Found");
        //when
        Exception exception = decoder.decode("key", response);
        //then
        assertThat(exception).isInstanceOf(NotFoundException.class)
                .hasMessage("쿠폰을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("500 응답이 오면 InternalServerException을 반환한다")
    void decodeWhen500Code(){
        //given
        Response response = createResponse(500, "Server Error");
        //when
        Exception exception = decoder.decode("key", response);
        //then
        assertThat(exception).isInstanceOf(InternalServerException.class)
                .hasMessage("쿠폰 서비스 장애 발생");
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
