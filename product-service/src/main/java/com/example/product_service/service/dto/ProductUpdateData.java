package com.example.product_service.service.dto;

import com.example.product_service.entity.Categories;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ProductUpdateData {
    Categories categories;
}
