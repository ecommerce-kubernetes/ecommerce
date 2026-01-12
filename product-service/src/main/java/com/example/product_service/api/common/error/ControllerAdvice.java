package com.example.product_service.api.common.error;

import com.example.product_service.api.common.error.dto.response.ErrorResponse;
import com.example.product_service.common.advice.ErrorResponseEntityFactory;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.exception.DuplicateResourceException;
import com.example.product_service.exception.NoPermissionException;
import com.example.product_service.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
@RequiredArgsConstructor
public class ControllerAdvice {
    private final ErrorResponseEntityFactory factory;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validationExceptionHandler(HttpServletRequest request,
                                                                              MethodArgumentNotValidException e){
        LocalDateTime now = LocalDateTime.now();
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String message = fieldErrors.get(0).getDefaultMessage();
        ErrorResponse errorResponse = createErrorResponse("VALIDATION", message, now.toString(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> notFoundExceptionHandler(HttpServletRequest request, NotFoundException e){
        return factory.toErrorResponseEntity(HttpStatus.NOT_FOUND, e.getMessage(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> accessDeniedExceptionHandler(HttpServletRequest request, AccessDeniedException e) {
        LocalDateTime now = LocalDateTime.now();
        ErrorResponse errorResponse = createErrorResponse("FORBIDDEN", "요청 권한이 없습니다", now.toString(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> badRequestExceptionHandler(HttpServletRequest request, BadRequestException e){
        return factory.toErrorResponseEntity(HttpStatus.BAD_REQUEST, e.getMessage(), request);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> conflictExceptionHandler(HttpServletRequest request, DuplicateResourceException e){
        return factory.toErrorResponseEntity(HttpStatus.CONFLICT, e.getMessage(), request);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> missingHeaderExceptionHandler(HttpServletRequest request, MissingRequestHeaderException e){
        return factory.toErrorResponseEntity(HttpStatus.BAD_REQUEST, e.getMessage(), request);
    }

    @ExceptionHandler(NoPermissionException.class)
    public ResponseEntity<ErrorResponse> noPermissionExceptionHandler(HttpServletRequest request, NoPermissionException e){
        return factory.toErrorResponseEntity(HttpStatus.FORBIDDEN, e.getMessage(), request);
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
