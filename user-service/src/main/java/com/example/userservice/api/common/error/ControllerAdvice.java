package com.example.userservice.api.common.error;

import com.example.userservice.api.common.error.dto.response.ErrorResponse;
import com.example.userservice.api.common.exception.BusinessException;
import com.example.userservice.api.common.exception.CommonErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validationExceptionHandler(HttpServletRequest request,
                                                                    MethodArgumentNotValidException e) {
        LocalDateTime now = LocalDateTime.now();
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String message = fieldErrors.get(0).getDefaultMessage();

        CommonErrorCode errorCode = CommonErrorCode.INVALID_INPUT_VALUE;

        ErrorResponse response = ErrorResponse.of(errorCode.getCode(), message, now.toString(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> constraintViolationExceptionHandler(HttpServletRequest request,
                                                                             ConstraintViolationException e){
        LocalDateTime now = LocalDateTime.now();
        String message = e.getConstraintViolations().iterator().next().getMessage();
        CommonErrorCode errorCode = CommonErrorCode.INVALID_INPUT_VALUE;
        ErrorResponse response = ErrorResponse.of(
                errorCode.getCode(),
                message,
                now.toString(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonErrors(HttpServletRequest request,
                                                          HttpMessageNotReadableException e) {
        LocalDateTime now = LocalDateTime.now();
        if (e.getMessage().contains("LocalDate")) {
            CommonErrorCode errorCode = CommonErrorCode.INVALID_DATE_FORMAT;
            ErrorResponse response = ErrorResponse.of(errorCode.getCode(), errorCode.getMessage(), now.toString(), request.getRequestURI());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        CommonErrorCode errorCode = CommonErrorCode.INVALID_TYPE_VALUE;
        ErrorResponse response = ErrorResponse.of(errorCode.getCode(), errorCode.getMessage(), now.toString(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> businessExceptionHandler(HttpServletRequest request, BusinessException e) {
        LocalDateTime now = LocalDateTime.now();
        ErrorResponse errorResponse = createErrorResponse(e.getErrorCode().getCode(), e.getErrorCode().getMessage(), now.toString(), request.getRequestURI());
        return ResponseEntity.status(e.getErrorCode().getStatus()).body(errorResponse);
    }

    private ErrorResponse createErrorResponse(String code, String message, String timestamp, String path){
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .timestamp(timestamp)
                .path(path)
                .build();
    }
}
