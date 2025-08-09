package com.example.product_service.common.advice;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.common.advice.dto.DetailError;
import com.example.product_service.common.advice.dto.ErrorResponse;
import com.example.product_service.common.advice.dto.ValidationErrorResponse;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.exception.DuplicateResourceException;
import com.example.product_service.exception.NoPermissionException;
import com.example.product_service.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class ControllerAdvice {
    private final MessageSourceUtil ms;
    private final ErrorResponseEntityFactory factory;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> validationExceptionHandler(HttpServletRequest request,
                                                                              MethodArgumentNotValidException e){
        return factory.toValidationErrorResponseEntity(HttpStatus.BAD_REQUEST, request, e);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> notFoundExceptionHandler(HttpServletRequest request, NotFoundException e){
        return factory.toErrorResponseEntity(HttpStatus.NOT_FOUND, e.getMessage(), request);
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
}
