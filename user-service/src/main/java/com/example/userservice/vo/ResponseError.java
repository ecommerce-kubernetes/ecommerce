package com.example.userservice.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "응답 에러 모델")
public class ResponseError {

    @Schema(description = "상태 정보")
    private int status;
    @Schema(description = "에러 정보")
    private String error;
    @Schema(description = "에러 메세지")
    private String message;
    @Schema(description = "에러 발생 시간")
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
