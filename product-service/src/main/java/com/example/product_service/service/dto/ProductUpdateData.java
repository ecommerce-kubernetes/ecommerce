package com.example.product_service.service.dto;

import com.example.product_service.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ProductUpdateData {
    Category category;
}
