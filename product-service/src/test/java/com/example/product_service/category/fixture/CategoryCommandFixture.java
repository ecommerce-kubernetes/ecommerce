package com.example.product_service.category.fixture;

import com.example.product_service.category.application.service.dto.command.CreateCategoryCommand;
import com.example.product_service.category.application.service.dto.command.UpdateCategoryCommand;

public class CategoryCommandFixture {

    public static CreateCategoryCommand.CreateCategoryCommandBuilder anCreateCategoryCommand() {
        return CreateCategoryCommand.builder()
                .name("채소")
                .parentId(null)
                .imagePath("/category/vegetable.jpg");
    }

    public static UpdateCategoryCommand.UpdateCategoryCommandBuilder anUpdateCategoryCommand() {
        return UpdateCategoryCommand.builder()
                .id(1L)
                .name("변경된 이름")
                .imagePath("/category/updated.jpg");
    }
}
