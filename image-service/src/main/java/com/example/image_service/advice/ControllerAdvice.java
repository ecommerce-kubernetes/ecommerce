package com.example.image_service.advice;

import com.example.image_service.dto.ExceptionResponse;
import com.example.image_service.exception.BadRequestException;
import com.example.image_service.exception.NotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ExceptionResponse> badRequestExceptionHandler(BadRequestException ex){
        ExceptionResponse response =
                buildExceptionResponse(HttpServletResponse.SC_BAD_REQUEST, "Bad Request", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ExceptionResponse> notFoundExceptionHandler(NotFoundException ex){
        ExceptionResponse response =
                buildExceptionResponse(HttpServletResponse.SC_NOT_FOUND, "Not Found", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    public ExceptionResponse buildExceptionResponse(int status, String error, String message){
        return ExceptionResponse.builder()
                .status(status)
                .error(error)
                .message(message).build();
    }
}
