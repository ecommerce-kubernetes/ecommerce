package com.example.order_service.api.cart.infrastructure.client.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class ItemOption {
    private String optionTypeName;
    private String optionValueName;

    @Builder
    private ItemOption(String optionTypeName, String optionValueName){
        this.optionTypeName = optionTypeName;
        this.optionValueName = optionValueName;
    }
}
