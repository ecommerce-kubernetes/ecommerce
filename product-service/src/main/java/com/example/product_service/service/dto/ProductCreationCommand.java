package com.example.product_service.service.dto;

import com.example.product_service.dto.request.product.ProductRequest;
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

    public ProductCreationCommand(ProductRequest request){
        this.name = request.getName();
        this.description = request.getDescription();
        this.imageUrls = request.getImages();
        this.optionTypeCommands = request.getProductOptionTypes()
                .stream().map(ProductOptionTypeCommand::new).toList();
        this.variantCommands = request.getProductVariants().stream().map(ProductVariantCommand::new).toList();
    }
}
