package com.example.order_service.api.cart.controller.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateQuantityRequest {
    @NotNull
    @Min(value = 1, message = "quantity는 1이상 이여야 합니다")
    private int quantity;

    @Builder
    private UpdateQuantityRequest(int quantity){
        this.quantity = quantity;
    }
}
