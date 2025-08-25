package com.example.product_service.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class ProductCreationCommand {
    private String name;
    private String description;
    private List<String> imageUrls;
    private List<ProductOptionTypeCommand> optionTypeCommands;
    private List<ProductVariantCommand> variantCommands;

}
