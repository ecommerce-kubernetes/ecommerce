package com.example.userservice.common.error;

import com.example.userservice.common.exception.BusinessException;
import com.example.userservice.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.List;

@RestControllerAdvice
@RequiredArgsConstructor
public class ControllerAdvice {

    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestCookieException(HttpServletRequest request,
                                                                             MissingRequestCookieException e) {
        ErrorResponse.InputError fieldError = ErrorResponse.InputError.of(e.getCookieName(), e.getCookieName() + "는 필수 입니다.");

        ErrorResponse response = ErrorResponse.ofValidation(List.of(fieldError), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(HttpServletRequest request,
                                                                    MethodArgumentNotValidException e) {
        List<ErrorResponse.InputError> fieldErrors = extractFieldErrors(e.getFieldErrors());

        ErrorResponse response = ErrorResponse.ofValidation(fieldErrors, request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidationException(HttpServletRequest request,
                                                                                          HandlerMethodValidationException e) {
        List<ErrorResponse.InputError> parameterErrors = extractParameterErrors(e.getParameterValidationResults());

        ErrorResponse response = ErrorResponse.ofValidation(parameterErrors, request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> businessExceptionHandler(HttpServletRequest request, BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();

        ErrorResponse response = ErrorResponse.of(errorCode.getCode(), errorCode.getMessage(), request.getRequestURI());

        return ResponseEntity.status(e.getErrorCode().getStatus()).body(response);
    }

    private List<ErrorResponse.InputError> extractFieldErrors(List<FieldError> fieldErrors) {
        return fieldErrors.stream().map(field -> ErrorResponse.InputError.of(field.getField(), field.getDefaultMessage())).toList();
    }

    private List<ErrorResponse.InputError> extractParameterErrors(List<ParameterValidationResult> parameterErrors) {
        return parameterErrors.stream().map(parameter -> ErrorResponse.InputError.of(parameter.getMethodParameter().getParameterName(),
                parameter.getResolvableErrors().getFirst().getDefaultMessage())).toList();
    }
}
