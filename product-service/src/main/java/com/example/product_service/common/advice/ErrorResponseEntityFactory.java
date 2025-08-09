package com.example.product_service.common.advice;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.common.advice.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ErrorResponseEntityFactory {
    private final MessageSourceUtil ms;

    public ResponseEntity<ErrorResponse> toResponseEntity(HttpStatus status, String detailMessage, HttpServletRequest request){
        if(status == HttpStatus.FORBIDDEN){
            return createResponseEntity(HttpStatus.FORBIDDEN, "forbidden", detailMessage, request);
        } else if (status == HttpStatus.BAD_REQUEST) {
            return createResponseEntity(HttpStatus.BAD_REQUEST, "badRequest", detailMessage, request);
        } else if (status == HttpStatus.CONFLICT){
            return createResponseEntity(HttpStatus.CONFLICT, "conflict", detailMessage, request);
        } else if (status == HttpStatus.NOT_FOUND) {
            return createResponseEntity(HttpStatus.NOT_FOUND, "notFound", detailMessage, request);
        }
        throw new IllegalArgumentException("Unsupported HttpStatus: " + status);
    }

    private ResponseEntity<ErrorResponse> createResponseEntity(HttpStatus status, String code, String detailMessage,
                                                               HttpServletRequest request){
        return ResponseEntity.status(status).body(create(code, detailMessage, request));
    }

    private ErrorResponse create(String errorCode, String detailMessage, HttpServletRequest request){
        String timestamp = LocalDateTime.now().toString();
        return new ErrorResponse(
                ms.getMessage(errorCode),
                detailMessage,
                timestamp,
                request.getRequestURI()
        );
    }
}
