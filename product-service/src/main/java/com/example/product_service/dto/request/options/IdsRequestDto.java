package com.example.product_service.dto.request.options;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IdsRequestDto {

    @NotEmpty(message = "ids must not be empty")
    private List<Long> ids;
}
