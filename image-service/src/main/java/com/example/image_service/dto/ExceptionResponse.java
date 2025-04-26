package com.example.image_service.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ExceptionResponse {
    private int status;
    private String error;
    private String message;
}
