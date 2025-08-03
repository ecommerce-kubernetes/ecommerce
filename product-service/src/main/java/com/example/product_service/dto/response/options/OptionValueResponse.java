package com.example.product_service.dto.response.options;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OptionValueResponse {
    private Long valueId;
    private Long typeId;
    private String value;
}
