package com.example.userservice.api.support;

import com.example.userservice.api.common.exception.BusinessException;
import com.example.userservice.api.common.exception.UserErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class DummyController {

    @GetMapping("/exception")
    public String throwBusinessException() {
        throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
    }

    @PostMapping("/not-readable")
    public String throwNotReadableException(@RequestBody TestRequestBody requestBody) {
        return "OK";
    }


    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class TestRequestBody {
        private LocalDateTime datetime;
        private Long number;
    }
}
