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
public class DefaultErrorDecoder implements ErrorDecoder {
    private final ObjectMapper objectMapper;
    @Override
    public Exception decode(String methodKey, Response response) {
        ErrorDecoder defaultDecoder = new Default();
        try {
            if (response.body() != null) {
                InputStream bodyInputStream = response.body().asInputStream();
                ClientErrorResponse errorResponse = objectMapper.readValue(bodyInputStream, ClientErrorResponse.class);
                if (response.status() >= 400 && response.status() < 500) {
                    return new ExternalClientException(errorResponse.getCode(), errorResponse.getMessage());
                }
                if (response.status() >= 500) {
                    return new ExternalServerException(errorResponse.getCode(), errorResponse.getMessage());
                }
            }
        } catch (IOException e) {
            log.error("에러 응답 파싱 실패", e);
        }
        return defaultDecoder.decode(methodKey, response);
    }
}
