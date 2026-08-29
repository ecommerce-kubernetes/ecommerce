package com.example.userservice.support;

import com.example.userservice.common.exception.BusinessException;
import com.example.userservice.common.exception.UserErrorCode;
import com.example.userservice.common.security.model.UserPrincipal;
import com.example.userservice.user.domain.vo.Role;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
