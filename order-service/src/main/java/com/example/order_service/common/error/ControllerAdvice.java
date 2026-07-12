package com.example.order_service.common.error;

import com.example.order_service.common.error.dto.response.ErrorResponse;
import com.example.order_service.common.error.dto.response.ValidationErrorResponse;
import com.example.order_service.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
@RequiredArgsConstructor
public class ControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> validationExceptionHandler(HttpServletRequest request,
                                                                    MethodArgumentNotValidException e){
        LocalDateTime now = LocalDateTime.now();
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();

        List<ValidationErrorResponse.FieldDetail> fieldDetails = fieldErrors.stream().map(
                field -> ValidationErrorResponse.FieldDetail.of(field.getField(), field.getDefaultMessage())
        ).toList();

        ValidationErrorResponse response = ValidationErrorResponse.builder()
                .code("VALIDATION")
                .message("입력값이 올바르지 않습니다.")
                .errors(fieldDetails)
                .timestamp(now)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> businessExceptionHandler(HttpServletRequest request, BusinessException e) {
        LocalDateTime now = LocalDateTime.now();
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(e.getErrorCode().getCode())
                .message(e.getErrorCode().getMessage())
                .timestamp(now.toString())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(e.getErrorCode().getStatus()).body(errorResponse);
    }

}
