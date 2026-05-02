package com.example.order_service.ordersheet.application.dto.command;

import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.common.exception.business.code.OrderSheetErrorCode;
import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderSheetCommand {

    @Builder
    public record Create(
            Long userId,
            List<OrderItem> items
    ) {
        public Create {
            if (items == null || items.isEmpty()) {
                throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_ITEM_REQUIRED);
            }
            //variantIds 중복 검사
            long uniqueItemCount = items.stream().map(OrderItem::productVariantId)
                    .distinct()
                    .count();
            if (uniqueItemCount != items.size()) {
                throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_DUPLICATE_ITEMS);
            }
        }

        public Map<Long, Integer> toQuantityMap() {
            return items.stream()
                    .collect(Collectors.toMap(OrderItem::productVariantId, OrderItem::quantity));
        }

        public List<Long> toProductVariantIds() {
            return items.stream().map(OrderItem::productVariantId).toList();
        }
    }

    @Builder
    public record OrderItem(
            Long productVariantId,
            Integer quantity
    ) {
    }
}
