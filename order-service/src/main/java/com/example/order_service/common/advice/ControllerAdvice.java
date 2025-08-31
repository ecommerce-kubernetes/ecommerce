package com.example.order_service.common.advice;

import com.example.order_service.common.MessagePath;
import com.example.order_service.common.MessageSourceUtil;
import com.example.order_service.common.advice.dto.DetailError;
import com.example.order_service.common.advice.dto.ErrorResponse;
import com.example.order_service.common.advice.dto.ValidationErrorResponse;
import com.example.order_service.exception.NotFoundException;
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

    private final MessageSourceUtil ms;

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

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> missingHeaderExceptionHandler(HttpServletRequest request, MissingRequestHeaderException e){
        String timestamp = LocalDateTime.now().toString();
        ErrorResponse response = new ErrorResponse(
                ms.getMessage(MessagePath.BAD_REQUEST),
                e.getMessage(),
                timestamp,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
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
}
