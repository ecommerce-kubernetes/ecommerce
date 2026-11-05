package com.example.product_service.common.error;

import com.example.product_service.common.error.dto.response.ErrorResponse;
import com.example.product_service.common.exception.BusinessException;
import com.example.product_service.common.exception.ErrorCategory;
import com.example.product_service.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@RequiredArgsConstructor
public class ControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(HttpServletRequest request,
                                                                                MethodArgumentNotValidException e) {
        List<ErrorResponse.InputError> fieldErrors = extractFieldErrors(e.getFieldErrors());

        ErrorResponse response = ErrorResponse.ofValidation(fieldErrors, request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> businessExceptionHandler(HttpServletRequest request, BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();

        ErrorResponse response = ErrorResponse.of(errorCode.getCode(), errorCode.getMessage(), request.getRequestURI());

        HttpStatus status = determineHttpStatus(errorCode.getCategory());
        return ResponseEntity.status(status).body(response);
    }

    private HttpStatus determineHttpStatus(ErrorCategory category) {
        return switch (category) {
            case INVALID_REQUEST -> HttpStatus.BAD_REQUEST;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case BUSINESS_CONFLICT -> HttpStatus.CONFLICT;
            case EXTERNAL_API_ERROR -> HttpStatus.SERVICE_UNAVAILABLE;
            case SYSTEM_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private List<ErrorResponse.InputError> extractFieldErrors(List<FieldError> fieldErrors) {
        return fieldErrors.stream().map(field -> ErrorResponse.InputError.of(field.getField(), field.getDefaultMessage())).toList();
    }
}
