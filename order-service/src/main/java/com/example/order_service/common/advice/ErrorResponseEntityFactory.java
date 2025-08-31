package com.example.order_service.common.advice;

import com.example.order_service.common.MessageSourceUtil;
import com.example.order_service.common.advice.dto.DetailError;
import com.example.order_service.common.advice.dto.ErrorResponse;
import com.example.order_service.common.advice.dto.ValidationErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ErrorResponseEntityFactory {
    private final MessageSourceUtil ms;

    public ResponseEntity<ValidationErrorResponse> toValidationErrorResponseEntity(HttpStatus status, HttpServletRequest request,
                                                                                   MethodArgumentNotValidException e){
        return createValidationErrorResponseEntity(status, request, e);
    }

    public ResponseEntity<ErrorResponse> toErrorResponseEntity(HttpStatus status, String detailMessage, HttpServletRequest request){
        if(status == HttpStatus.FORBIDDEN){
            return createErrorResponseEntity(HttpStatus.FORBIDDEN, "forbidden", detailMessage, request);
        } else if (status == HttpStatus.BAD_REQUEST) {
            return createErrorResponseEntity(HttpStatus.BAD_REQUEST, "badRequest", detailMessage, request);
        } else if (status == HttpStatus.NOT_FOUND) {
            return createErrorResponseEntity(HttpStatus.NOT_FOUND, "notFound", detailMessage, request);
        }
        throw new IllegalArgumentException("Unsupported HttpStatus: " + status);
    }

    private ResponseEntity<ErrorResponse> createErrorResponseEntity(HttpStatus status, String code, String detailMessage,
                                                                    HttpServletRequest request){
        return ResponseEntity.status(status).body(create(code, detailMessage, request));
    }

    private ResponseEntity<ValidationErrorResponse> createValidationErrorResponseEntity(HttpStatus status, HttpServletRequest request,
                                                                                        MethodArgumentNotValidException e){
        List<DetailError> detailError = createDetailError(e);
        ValidationErrorResponse response = createValidationErrorResponse(detailError, request);
        return ResponseEntity.status(status).body(response);
    }

    private ErrorResponse create(String errorCode, String detailMessage, HttpServletRequest request){
        String timestamp = LocalDateTime.now().toString();
        return new ErrorResponse(
                ms.getMessage(errorCode),
                detailMessage,
                timestamp,
                request.getRequestURI()
        );
    }

    private ValidationErrorResponse createValidationErrorResponse(List<DetailError> detailErrors, HttpServletRequest request){
        String timestamp = LocalDateTime.now().toString();
        return new ValidationErrorResponse(
                ms.getMessage("badRequest"),
                ms.getMessage("badRequest.validation"),
                detailErrors,
                timestamp,
                request.getRequestURI()
        );
    }

    private List<DetailError> createDetailError(MethodArgumentNotValidException e){
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        return  fieldErrors.stream().map(
                (fieldError) -> new DetailError(fieldError.getField(), fieldError.getDefaultMessage())
        ).toList();
    }
}
