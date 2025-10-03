package com.example.order_service.common.advice.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ValidationErrorResponse {
    private String error;
    private String message;
    private List<DetailError> errors;
    private String timestamp;
    private String path;

}