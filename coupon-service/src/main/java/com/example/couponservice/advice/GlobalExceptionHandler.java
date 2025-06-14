package com.example.couponservice.advice;

import com.example.couponservice.advice.exceptions.InvalidPhoneNumberException;
import com.example.couponservice.advice.exceptions.IsExistCouponException;
import com.example.couponservice.vo.ResponseError;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 권한이 부족할 때 발생
     */
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ResponseError> handleAuthorizationDenied(AuthorizationDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ResponseError.of(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."));
    }

    /**
     * 쿠폰을 찾을 수 없을 때 발생
     */
    @ExceptionHandler(IsExistCouponException.class)
    public ResponseEntity<ResponseError> handleUsernameNotFound(IsExistCouponException ex) {
        log.warn("Coupon not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseError.of(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    /**
     * 핸드폰 번호 정보가 유효하지 않을 때 발생
     */
    @ExceptionHandler(InvalidPhoneNumberException.class)
    public ResponseEntity<ResponseError> handleUsernameNotFound(InvalidPhoneNumberException ex) {
        log.warn("PhoneNumberData not valid: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseError.of(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    /**
     * @RequestParam, @PathVariable, @Validated 유효성 검사 실패 시 발생
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ResponseError> handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn("Constraint violation: {}", ex.getMessage());

        String errorMessage = ex.getConstraintViolations().stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse("요청값이 유효하지 않습니다.");

        return ResponseEntity.badRequest()
                .body(ResponseError.of(HttpStatus.BAD_REQUEST, errorMessage));
    }

    /**
     * RequestBody 유효성 검사 실패 시 발생
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseError> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}", ex.getMessage());

        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("유효성 검사에 실패했습니다.");

        return ResponseEntity.badRequest()
                .body(ResponseError.of(HttpStatus.BAD_REQUEST, errorMessage));
    }

    /**
     * 그 외 모든 예외에 대한 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseError> handleAllOtherExceptions(Exception ex) {
        log.error("Unhandled exception occurred", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseError.of(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 오류가 발생했습니다."));
    }

}
