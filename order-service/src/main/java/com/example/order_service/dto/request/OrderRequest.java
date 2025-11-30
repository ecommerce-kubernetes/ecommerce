package com.example.order_service.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class OrderRequest {
    @NotEmpty(message = "주문 상품은 필수입니다")
    @Valid
    private List<OrderItemRequest> items;
    @NotBlank(message = "배송지는 필수입니다")
    private String deliveryAddress;
    private Long couponId;
    @NotNull(message = "사용할 포인트는 필수입니다")
    @Min(value = 0, message = "사용할 포인트는 0원 이상이여야 합니다")
    private Long pointToUse;
    @NotNull(message = "예상 결제 금액은 필수입니다")
    private Long expectedPrice;

    public Map<Long, Integer> toQuantityMap(){
        return items.stream().collect(
                Collectors.toMap(
                    OrderItemRequest::getProductVariantId,
                    OrderItemRequest::getQuantity
                )
        );
    }

    @Builder
    private OrderRequest(List<OrderItemRequest> items, String deliveryAddress, Long couponId, Long pointToUse, Long expectedPrice){
        this.items = items;
        this.deliveryAddress = deliveryAddress;
        this.couponId = couponId;
        this.pointToUse = pointToUse;
        this.expectedPrice = expectedPrice;
    }

    @JsonIgnore
    public List<Long> getItemsVariantId(){
        return items.stream().map(OrderItemRequest::getProductVariantId).toList();
    }
}
