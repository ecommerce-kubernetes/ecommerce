package com.example.order_service.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
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
    @NotEmpty(message = "{NotEmpty}")
    @Valid
    private List<OrderItemRequest> items;
    @NotBlank(message = "{NotBlank}")
    private String deliveryAddress;
    private Long couponId;
    private Long pointToUse;
    @NotNull(message = "{NotNull}")
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
