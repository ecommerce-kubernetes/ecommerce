package com.example.product_service.common.advice;

import com.example.product_service.common.advice.dto.DetailError;
import com.example.product_service.common.advice.dto.ErrorResponse;
import com.example.product_service.common.advice.dto.ValidationErrorResponse;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.exception.DuplicateResourceException;
import com.example.product_service.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> validationExceptionHandler(HttpServletRequest request,
                                                                              MethodArgumentNotValidException e){
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        List<DetailError> detailErrors = fieldErrors.stream()
                .map(
                        (fieldError) -> new DetailError(fieldError.getField(), fieldError.getDefaultMessage())
                ).toList();


        String timestamp = LocalDateTime.now().toString();
        ValidationErrorResponse validationErrorResponse = new ValidationErrorResponse(
                "BadRequest",
                "Validation Error",
                detailErrors,
                timestamp,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationErrorResponse);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> notFoundExceptionHandler(HttpServletRequest request, NotFoundException e){
        String timestamp = LocalDateTime.now().toString();
        ErrorResponse errorResponse = new ErrorResponse(
                "NotFound",
                e.getMessage(),
                timestamp,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> badRequestExceptionHandler(HttpServletRequest request, BadRequestException e){
        String timestamp = LocalDateTime.now().toString();
        ErrorResponse errorResponse = new ErrorResponse(
                "BadRequest",
                e.getMessage(),
                timestamp,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> conflictExceptionHandler(HttpServletRequest request, DuplicateResourceException e){
        String timestamp = LocalDateTime.now().toString();
        ErrorResponse errorResponse = new ErrorResponse(
                "Conflict",
                e.getMessage(),
                timestamp,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
}
