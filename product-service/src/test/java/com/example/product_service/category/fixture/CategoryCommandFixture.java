package com.example.product_service.category.fixture;

import com.example.product_service.category.application.service.dto.command.CreateCategoryCommand;

public class CategoryCommandFixture {

    public static CreateCategoryCommand.CreateCategoryCommandBuilder anCreateCategoryCommand() {
        return CreateCategoryCommand.builder()
                .name("채소")
                .parentId(null)
                .imagePath("/category/vegetable.jpg");
    }
}
