package com.example.userservice.api.common.error;

import com.example.userservice.api.common.error.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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
    public ResponseEntity<ErrorResponse> validationExceptionHandler(HttpServletRequest request,
                                                                    MethodArgumentNotValidException e) {
        LocalDateTime now = LocalDateTime.now();
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String message = fieldErrors.get(0).getDefaultMessage();
        ErrorResponse response = ErrorResponse.of("VALIDATION", message, now.toString(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonErrors(HttpServletRequest request,
                                                          HttpMessageNotReadableException e) {
        LocalDateTime now = LocalDateTime.now();
        if (e.getMessage().contains("LocalDate")) {
            ErrorResponse response = ErrorResponse.of("VALIDATION", "잘못된 날짜 형식입니다", now.toString(), request.getRequestURI());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        ErrorResponse response = ErrorResponse.of("VALIDATION", "요청 데이터 형식이 올바르지 않습니다", now.toString(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
