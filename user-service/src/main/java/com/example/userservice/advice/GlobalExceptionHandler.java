package com.example.userservice.advice;

import com.example.userservice.advice.exceptions.InvalidAmountException;
import com.example.userservice.advice.exceptions.InvalidPasswordException;
import com.example.userservice.advice.exceptions.RefreshTokenNotFoundException;
import com.example.userservice.advice.exceptions.UserNotFoundException;
import com.example.userservice.vo.ResponseError;
import jakarta.persistence.EntityNotFoundException;
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
     * RefreshToken이 존재하지 않을 때 발생
     */
    @ExceptionHandler(RefreshTokenNotFoundException.class)
    public ResponseEntity<ResponseError> handleRefreshTokenNotFound(RefreshTokenNotFoundException ex) {
        log.warn("Refresh token not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ResponseError.of(HttpStatus.UNAUTHORIZED, ex.getMessage()));
    }

    /**
     * 이메일로 사용자를 찾을 수 없을 때 발생
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ResponseError> handleUsernameNotFound(UserNotFoundException ex) {
        log.warn("User not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseError.of(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    /**
     * JPA Entity가 존재하지 않을 때 발생
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ResponseError> handleEntityNotFound(EntityNotFoundException ex) {
        log.warn("Entity not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseError.of(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    /**
     * 상태 불일치 등의 예외 처리
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ResponseError> handleIllegalState(IllegalStateException ex) {
        log.warn("Illegal state: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ResponseError.of(HttpStatus.CONFLICT, ex.getMessage()));
    }

    /**
     * 비밀번호가 일치하지 않을 때 발생
     */
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ResponseError> handleInvalidPassword(InvalidPasswordException ex) {
        log.warn("Invalid password: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ResponseError.of(HttpStatus.UNAUTHORIZED, ex.getMessage()));
    }

    /**
     * 잘못된 금액 등의 유효성 검증 실패 시 발생
     */
    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<ResponseError> handleInvalidAmount(InvalidAmountException ex) {
        log.warn("Invalid amount: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseError.of(HttpStatus.BAD_REQUEST, ex.getMessage()));
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
