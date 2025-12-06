package com.example.order_service.api.common.error;

import com.example.order_service.api.common.exception.*;
import com.example.order_service.api.common.error.dto.response.ErrorResponse;
import com.example.order_service.api.common.exception.server.InternalServerException;
import com.example.order_service.api.common.exception.server.UnavailableServiceException;
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
    public ResponseEntity<ErrorResponse> validationExceptionHandler(HttpServletRequest request,
                                                                    MethodArgumentNotValidException e){
        LocalDateTime now = LocalDateTime.now();
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String message = fieldErrors.get(0).getDefaultMessage();
        ErrorResponse response = ErrorResponse.toBadRequest(message, now.toString(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> notFoundExceptionHandler(HttpServletRequest request, NotFoundException e){
        LocalDateTime now = LocalDateTime.now();
        String message = e.getMessage();
        ErrorResponse response = ErrorResponse.toNotFound(message, now.toString(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(UnavailableServiceException.class)
    public ResponseEntity<ErrorResponse> unAvailableServerExceptionHandler(HttpServletRequest request, UnavailableServiceException e){
        LocalDateTime now = LocalDateTime.now();
        String message = e.getMessage();
        ErrorResponse response = ErrorResponse.toUnavailableServer(message, now.toString(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<ErrorResponse> internalServerExceptionHandler(HttpServletRequest request, InternalServerException e){
        LocalDateTime now = LocalDateTime.now();
        String message = e.getMessage();
        ErrorResponse response = ErrorResponse.toInternalServerError(message, now.toString(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(InsufficientException.class)
    public ResponseEntity<ErrorResponse> insufficientExceptionHandler(HttpServletRequest request, InsufficientException e){
        LocalDateTime now = LocalDateTime.now();
        String message = e.getMessage();
        ErrorResponse response = ErrorResponse.toConflict(message, now.toString(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(InvalidQuantityException.class)
    public ResponseEntity<ErrorResponse> invalidQuantityExceptionHandler(HttpServletRequest request, InvalidQuantityException e){
        LocalDateTime now = LocalDateTime.now();
        String message = e.getMessage();
        ErrorResponse response = ErrorResponse.toBadRequest(message, now.toString(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> badRequestExceptionHandler(HttpServletRequest request, BadRequestException e){
        LocalDateTime now = LocalDateTime.now();
        String message = e.getMessage();
        ErrorResponse response = ErrorResponse.toBadRequest(message, now.toString(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(NoPermissionException.class)
    public ResponseEntity<ErrorResponse> noPermissionExceptionHandler(HttpServletRequest request, NoPermissionException e){
        LocalDateTime now = LocalDateTime.now();
        String message = e.getMessage();
        ErrorResponse response = ErrorResponse.toNoPermission(message, now.toString(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
}
