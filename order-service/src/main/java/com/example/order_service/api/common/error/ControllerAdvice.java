package com.example.order_service.api.common.error;

import com.example.order_service.api.common.error.dto.response.ErrorResponse;
import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.NoPermissionException;
import com.example.order_service.api.common.exception.NotFoundException;
import com.example.order_service.api.common.exception.PaymentException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
                                                                    MethodArgumentNotValidException e){
        LocalDateTime now = LocalDateTime.now();
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String message = fieldErrors.get(0).getDefaultMessage();
        ErrorResponse response = ErrorResponse.builder()
                .code("VALIDATION")
                .message(message)
                .timestamp(now.toString())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> notFoundExceptionHandler(HttpServletRequest request, NotFoundException e){
        LocalDateTime now = LocalDateTime.now();
        String message = e.getMessage();
        ErrorResponse response = ErrorResponse.toNotFound(message, now.toString(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(NoPermissionException.class)
    public ResponseEntity<ErrorResponse> noPermissionExceptionHandler(HttpServletRequest request, NoPermissionException e){
        LocalDateTime now = LocalDateTime.now();
        String message = e.getMessage();
        ErrorResponse response = ErrorResponse.toNoPermission(message, now.toString(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> accessDeniedExceptionHandler(HttpServletRequest request, AccessDeniedException e){
        LocalDateTime now = LocalDateTime.now();
        String message = "권한이 부족합니다";
        ErrorResponse response = ErrorResponse.toNoPermission(message, now.toString(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ErrorResponse> paymentExceptionHandler(HttpServletRequest request, PaymentException e) {
        LocalDateTime now = LocalDateTime.now();
        ErrorResponse response = ErrorResponse.toBadRequest(e.getMessage(), now.toString(), request.getRequestURI());
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
