package com.example.product_service.common.advice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DetailError {
    private String fieldName;
    private String message;

    public DetailError(String fieldName, String message){
        this.fieldName = fieldName;
        this.message = message;
    }
}
