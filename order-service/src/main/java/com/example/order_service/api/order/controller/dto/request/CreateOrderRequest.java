package com.example.order_service.api.order.controller.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CreateOrderRequest {
    @NotEmpty(message = "주문 상품은 필수입니다")
    @Valid
    private List<CreateOrderItemRequest> items;
    @NotBlank(message = "배송지는 필수입니다")
    private String deliveryAddress;
    private Long couponId;
    @NotNull(message = "사용할 포인트는 필수입니다")
    @Min(value = 0, message = "사용할 포인트는 0 이상이여야 합니다")
    private Long pointToUse;
    @NotNull(message = "예상 결제 금액은 필수입니다")
    @Min(value = 1, message = "예상 결제 금액은 1 이상이여야 합니다")
    private Long expectedPrice;

    @Builder
    private CreateOrderRequest(List<CreateOrderItemRequest> items, String deliveryAddress, Long couponId, Long pointToUse, Long expectedPrice){
        this.items = items;
        this.deliveryAddress = deliveryAddress;
        this.couponId = couponId;
        this.pointToUse = pointToUse;
        this.expectedPrice = expectedPrice;
    }
}
