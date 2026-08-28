package com.example.userservice.user.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailAvailableResponse {
    private boolean available;
}
