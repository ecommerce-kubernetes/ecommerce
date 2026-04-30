package com.example.order_service.infrastructure.decoder;

import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.infrastructure.dto.response.ClientErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@RequiredArgsConstructor
public class ProductErrorDecoder implements ErrorDecoder {
    private final ObjectMapper objectMapper;
    @Override
    public Exception decode(String methodKey, Response response) {
        ErrorDecoder defaultDecoder = new Default();
        try {
            // 응답 바디가 있는경우
            if (response.body() != null) {
                InputStream bodyInputStream = response.body().asInputStream();
                ClientErrorResponse errorResponse = objectMapper.readValue(bodyInputStream, ClientErrorResponse.class);

                log.error("Product 서비스 에러 - Method: {}, Status: {}, Code: {}, Message: {}",
                        methodKey, response.status(), errorResponse.getCode(), errorResponse.getMessage());
                // 에러가 클라이언트 에러인 경우 (400 ~ 500)
                if (response.status() >= 400 && response.status() < 500) {
                    return new ExternalClientException(errorResponse.getMessage());
                }
                // 에러가 서버 에러인 경우 (500~)
                if (response.status() >= 500) {
                    return new ExternalServerException(errorResponse.getMessage());
                }
            }
        } catch (IOException e) {
            // 바디 파싱 실패시
            log.error("에러 응답 파싱 실패", e);
        }

        //바디가 없는 경우, 파싱 실패, 알 수 없는 에러 발생시 기본 디코더 호출
        return defaultDecoder.decode(methodKey, response);
    }
}
