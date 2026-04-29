package com.example.order_service.ordersheet.infrastructure.persistence;

import com.example.order_service.ordersheet.domain.OrderSheet;
import com.example.order_service.ordersheet.domain.OrderSheetRepository;
import org.springframework.stereotype.Repository;

@Repository
public class OrderSheetRepositoryImpl implements OrderSheetRepository {
    @Override
    public OrderSheet save(OrderSheet orderSheet) {
        return null;
    }
}
