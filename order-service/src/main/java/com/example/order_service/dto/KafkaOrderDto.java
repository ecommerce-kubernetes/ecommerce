package com.example.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class KafkaOrderDto {
    private Long id;
    private List<KafkaOrderItemDto> orderItemDtoList;
}
