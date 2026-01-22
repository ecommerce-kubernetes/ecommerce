package com.example.product_service.api.common.error;

import com.example.product_service.api.common.error.dto.response.ErrorResponse;
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
        ErrorResponse errorResponse = createErrorResponse("VALIDATION", message, now.toString(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
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
