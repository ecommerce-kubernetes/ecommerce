package com.example.userservice.api.user.controller.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailAvailableResponse {
    private boolean available;
}
