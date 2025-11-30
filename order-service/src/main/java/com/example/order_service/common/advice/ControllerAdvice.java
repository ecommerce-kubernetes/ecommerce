package com.example.order_service.common.advice;

import com.example.order_service.common.advice.dto.ErrorResponse;
import com.example.order_service.common.advice.dto.ValidationErrorResponse;
import com.example.order_service.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<ValidationErrorResponse> validationExceptionHandler(HttpServletRequest request,
//                                                                              MethodArgumentNotValidException e){
//        return factory.toValidationErrorResponseEntity(HttpStatus.BAD_REQUEST, request, e);
//    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validationExceptionHandler(HttpServletRequest request,
                                                                    MethodArgumentNotValidException e){
        LocalDateTime now = LocalDateTime.now();
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String errorMessage = fieldErrors.get(0).getDefaultMessage();
        ErrorResponse response = ErrorResponse.toBadRequest(errorMessage, now.toString(), request.getRequestURI());
        return ResponseEntity.badRequest().body(response);

    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> missingHeaderExceptionHandler(HttpServletRequest request, MissingRequestHeaderException e){
        return factory.toErrorResponseEntity(HttpStatus.BAD_REQUEST, e.getMessage(), request);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> notFoundExceptionHandler(HttpServletRequest request, NotFoundException e){
        return factory.toErrorResponseEntity(HttpStatus.NOT_FOUND, e.getMessage(), request);
    }

    @ExceptionHandler(InsufficientException.class)
    public ResponseEntity<ErrorResponse> insufficientExceptionHandler(HttpServletRequest request, InsufficientException e){
        return factory.toErrorResponseEntity(HttpStatus.CONFLICT, e.getMessage(), request);
    }

    @ExceptionHandler(InvalidResourceException.class)
    public ResponseEntity<ErrorResponse> invalidResourceExceptionHandler(HttpServletRequest request, InvalidResourceException e){
        return factory.toErrorResponseEntity(HttpStatus.CONFLICT, e.getMessage(), request);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> badRequestExceptionHandler(HttpServletRequest request, BadRequestException e){
        return factory.toErrorResponseEntity(HttpStatus.BAD_REQUEST, e.getMessage(), request);
    }

    @ExceptionHandler(NoPermissionException.class)
    public ResponseEntity<ErrorResponse> noPermissionExceptionHandler(HttpServletRequest request, NoPermissionException e){
        return factory.toErrorResponseEntity(HttpStatus.FORBIDDEN, e.getMessage(), request);
    }
}
