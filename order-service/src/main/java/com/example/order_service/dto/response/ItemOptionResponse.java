package com.example.order_service.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class ItemOptionResponse {
    private String optionTypeName;
    private String optionValueName;

    @Builder
    private ItemOptionResponse(String optionTypeName, String optionValueName){
        this.optionTypeName = optionTypeName;
        this.optionValueName = optionValueName;
    }
}
