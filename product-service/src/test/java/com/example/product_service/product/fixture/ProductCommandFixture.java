package com.example.product_service.product.fixture;

import com.example.product_service.product.application.service.dto.command.CreateProductCommand;

public class ProductCommandFixture {

    public static CreateProductCommand.CreateProductCommandBuilder anCreateProductCommand() {
        return CreateProductCommand.builder()
                .name("상품")
                .categoryId(1L)
                .description("상품 설명");
    }
}
