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

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
@RequiredArgsConstructor
public class ControllerAdvice {
    private final MessageSourceUtil ms;
    private final ErrorResponseEntityFactory factory;

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
                ms.getMessage("badRequest"),
                ms.getMessage("badRequest.validation"),
                detailErrors,
                timestamp,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationErrorResponse);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> notFoundExceptionHandler(HttpServletRequest request, NotFoundException e){
        return factory.toResponseEntity(HttpStatus.NOT_FOUND, e.getMessage(), request);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> badRequestExceptionHandler(HttpServletRequest request, BadRequestException e){
        return factory.toResponseEntity(HttpStatus.BAD_REQUEST, e.getMessage(), request);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> conflictExceptionHandler(HttpServletRequest request, DuplicateResourceException e){
        return factory.toResponseEntity(HttpStatus.CONFLICT, e.getMessage(), request);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> missingHeaderExceptionHandler(HttpServletRequest request, MissingRequestHeaderException e){
        ErrorResponse errorResponse = createErrorResponse("badRequest", e.getHeaderName() + ms.getMessage("Header-Missing"),
                request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(NoPermissionException.class)
    public ResponseEntity<ErrorResponse> noPermissionExceptionHandler(HttpServletRequest request, NoPermissionException e){
        return factory.toResponseEntity(HttpStatus.FORBIDDEN, e.getMessage(), request);
    }

    private ErrorResponse createErrorResponse(String errorCode, String detailMessage, HttpServletRequest request){
        String timestamp = LocalDateTime.now().toString();
        return new ErrorResponse(
                ms.getMessage(errorCode),
                detailMessage,
                timestamp,
                request.getRequestURI()
        );
    }
}
