package com.example.product_service.product.fixture;

import com.example.product_service.product.application.service.dto.command.CreateProductCommand;
import com.example.product_service.product.application.service.dto.command.UpdateProductCommand;

public class ProductCommandFixture {

    public static CreateProductCommand.CreateProductCommandBuilder anCreateProductCommand() {
        return CreateProductCommand.builder()
                .name("상품")
                .categoryId(1L)
                .description("상품 설명");
    }

    public static UpdateProductCommand.UpdateProductCommandBuilder anUpdateProductCommand() {
        return UpdateProductCommand.builder()
                .productId(1L)
                .name("변경된 상품")
                .categoryId(2L)
                .description("변경된 상품 설명");
    }
}
