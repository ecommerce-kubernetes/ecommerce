package com.example.couponservice.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResponseError {
    private int status;
    private String error;
    private String message;
    private LocalDateTime timestamp;

    public static ResponseError of(HttpStatus status, String message) {
        return new ResponseError(
                status.value(),
                status.getReasonPhrase(),
                message,
                LocalDateTime.now()
        );
    }
}
